package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxDocument
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxKeyLockDocument
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.repository.MongoEventInboxRepository
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import java.time.Instant

@ManagedRepository
open class MongoEventInboxStore(
    private val repository: MongoEventInboxRepository,
    private val mongoTemplate: MongoTemplate,
) : EventInboxStore {

    override fun save(inbox: EventInbox): EventInbox {
        repository.save(EventInboxDocument.from(inbox))
        return inbox
    }

    override fun tryAcquire(eventId: Long): Boolean {
        val result = mongoTemplate.findAndModify(
            Query(Criteria.where("_id").`is`(eventId).and("status").`is`(EventInboxStatus.RECEIVED)),
            Update().set("status", EventInboxStatus.PROCESSING)
                .inc("attempt_count", 1)
                .set("last_attempted_at", Instant.now()),
            FindAndModifyOptions.options().returnNew(false),
            EventInboxDocument::class.java,
        )
        return result != null
    }

    override fun markCompleted(eventId: Long) {
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", EventInboxStatus.COMPLETED).set("processed_at", Instant.now()),
            EventInboxDocument::class.java,
        )
    }

    override fun markFailed(eventId: Long, error: String) {
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", EventInboxStatus.FAILED)
                .set("error_message", error)
                .set("last_attempted_at", Instant.now()),
            EventInboxDocument::class.java,
        )
    }

    override fun markDead(eventId: Long) {
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", EventInboxStatus.DEAD).set("last_attempted_at", Instant.now()),
            EventInboxDocument::class.java,
        )
    }

    override fun findById(eventId: Long): EventInbox? = repository.findById(eventId).orElse(null)

    override fun findAllByEventTypeAndStatus(eventType: String, status: EventInboxStatus, limit: Int): List<EventInbox> {
        val query = Query(Criteria.where("event_type").`is`(eventType).and("status").`is`(status))
            .with(Sort.by("occurred_at").ascending())
            .limit(limit)
        return mongoTemplate.find(query, EventInboxDocument::class.java)
    }

    override fun resetStaleProcessing(eventType: String, olderThan: Instant): Int {
        val result = mongoTemplate.updateMulti(
            Query(Criteria.where("event_type").`is`(eventType)
                .and("status").`is`(EventInboxStatus.PROCESSING)
                .and("last_attempted_at").lt(olderThan)),
            Update().set("status", EventInboxStatus.RECEIVED),
            EventInboxDocument::class.java,
        )
        return result.modifiedCount.toInt()
    }

    // ---------- PARTITION_ORDERED (partition-ordered-design.md) ----------

    companion object {
        /** 크래시로 delete 가 누락된 claim 락 문서의 TTL(초). 정상 경로 락 수명은 밀리초. */
        private const val KEY_LOCK_TTL_SECONDS = 60L
    }

    /** claim 락 컬렉션의 TTL 인덱스를 최초 사용 시 1회 보장(멱등). */
    private val keyLockTtlIndexEnsured: Boolean by lazy {
        mongoTemplate.indexOps(EventInboxKeyLockDocument::class.java)
            .createIndex(
                Index().on("locked_at", Sort.Direction.ASC)
                    .expire(KEY_LOCK_TTL_SECONDS)
                    .named("ttl_event_inbox_key_locks_locked_at")
            )
        true
    }

    override fun findClaimableFrontiers(eventType: String, limit: Int): List<EventInbox> {
        // 게이트는 키 전역(D2) — event_type 무관하게 PROCESSING/FAILED 보유 키를 제외한다.
        val blockedKeys = mongoTemplate.findDistinct(
            Query(Criteria.where("status").`in`(EventInboxStatus.PROCESSING, EventInboxStatus.FAILED, EventInboxStatus.DEAD)),
            "partition_key",
            EventInboxDocument::class.java,
            Long::class.java,
        )

        // 키당 최선두 1건(sort 후 group-first), 최고령 순 limit — 한 키 백로그의 윈도우 침수 방지.
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("event_type").`is`(eventType)
                    .and("status").`is`(EventInboxStatus.RECEIVED)
                    .and("partition_key").nin(blockedKeys)
            ),
            Aggregation.sort(Sort.Direction.ASC, "_id"),
            Aggregation.group("partition_key").first(Aggregation.ROOT).`as`("doc"),
            Aggregation.replaceRoot("doc"),
            Aggregation.sort(Sort.Direction.ASC, "_id"),
            Aggregation.limit(limit.toLong()),
        )
        return mongoTemplate.aggregate(
            aggregation,
            EventInboxDocument::class.java,
            EventInboxDocument::class.java,
        ).mappedResults
    }

    /**
     * 키 락(insert) → 게이트 검사 → 마킹 → 락 해제(delete).
     * 락은 claim 순간만 유지되고, 처리 기간의 상호배제는 PROCESSING 상태가 담당한다.
     */
    override fun touchProcessing(eventIds: List<Long>) {
        if (eventIds.isEmpty()) return
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(eventIds).and("status").`is`(EventInboxStatus.PROCESSING)),
            Update().set("last_attempted_at", Instant.now()),
            EventInboxDocument::class.java,
        )
    }

    override fun markReceivedForRetry(eventId: Long): Boolean {
        val result = mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId).and("status").`is`(EventInboxStatus.FAILED)),
            Update().set("status", EventInboxStatus.RECEIVED),
            EventInboxDocument::class.java,
        )
        return result.modifiedCount > 0
    }

    override fun tryAcquireOrdered(eventId: Long, partitionKey: Long): Boolean {
        keyLockTtlIndexEnsured

        // ① 키 락 획득 — _id 유니크가 원자성 담당. 중복 = 타 인스턴스가 claim 중.
        try {
            mongoTemplate.insert(EventInboxKeyLockDocument(partitionKey = partitionKey, lockedAt = Instant.now()))
        } catch (e: DuplicateKeyException) {
            return false
        }

        try {
            // ② 게이트 — 같은 키에 PROCESSING/FAILED 존재 시 claim 불가(FAILED = 키 블로킹).
            val blocked = mongoTemplate.exists(
                Query(Criteria.where("partition_key").`is`(partitionKey)
                    .and("status").`in`(EventInboxStatus.PROCESSING, EventInboxStatus.FAILED, EventInboxStatus.DEAD)),
                EventInboxDocument::class.java,
            )
            if (blocked) {
                return false
            }

            // ③ 마킹 — RECEIVED → PROCESSING (단일 문서 원자 연산)
            val result = mongoTemplate.findAndModify(
                Query(Criteria.where("_id").`is`(eventId).and("status").`is`(EventInboxStatus.RECEIVED)),
                Update().set("status", EventInboxStatus.PROCESSING)
                    .inc("attempt_count", 1)
                    .set("last_attempted_at", Instant.now()),
                FindAndModifyOptions.options().returnNew(false),
                EventInboxDocument::class.java,
            )
            return result != null
        } finally {
            // ④ 락 해제 — 성공/실패 불문 즉시. 누락 시 TTL 이 청소.
            mongoTemplate.remove(
                Query(Criteria.where("_id").`is`(partitionKey)),
                EventInboxKeyLockDocument::class.java,
            )
        }
    }

}
