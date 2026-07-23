package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxDocument
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxKeyLockDocument
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import java.time.Instant

@ManagedRepository
open class ReactiveMongoEventInboxStore(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : EventInboxStore {

    override fun save(inbox: EventInbox): EventInbox {
        reactiveMongoTemplate.save(EventInboxDocument.from(inbox)).block()
        return inbox
    }

    override fun tryAcquire(eventId: Long): Boolean {
        val result = reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("_id").`is`(eventId).and("status").`is`(EventInboxStatus.RECEIVED)),
            Update().set("status", EventInboxStatus.PROCESSING)
                .inc("attempt_count", 1)
                .set("last_attempted_at", Instant.now()),
            FindAndModifyOptions.options().returnNew(false),
            EventInboxDocument::class.java,
        ).block()
        return result != null
    }

    override fun markCompleted(eventId: Long) {
        reactiveMongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", EventInboxStatus.COMPLETED).set("processed_at", Instant.now()),
            EventInboxDocument::class.java,
        ).block()
    }

    override fun markFailed(eventId: Long, error: String) {
        reactiveMongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", EventInboxStatus.FAILED)
                .set("error_message", error)
                .set("last_attempted_at", Instant.now()),
            EventInboxDocument::class.java,
        ).block()
    }

    override fun markDead(eventId: Long) {
        reactiveMongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", EventInboxStatus.DEAD).set("last_attempted_at", Instant.now()),
            EventInboxDocument::class.java,
        ).block()
    }

    override fun findById(eventId: Long): EventInbox? =
        reactiveMongoTemplate.findById(eventId, EventInboxDocument::class.java).block()

    override fun findAllByEventTypeAndStatus(eventType: String, status: EventInboxStatus, limit: Int): List<EventInbox> {
        val query = Query(Criteria.where("event_type").`is`(eventType).and("status").`is`(status))
            .with(Sort.by("occurred_at").ascending())
            .limit(limit)
        return reactiveMongoTemplate.find(query, EventInboxDocument::class.java)
            .collectList().block() ?: emptyList()
    }

    override fun resetStaleProcessing(eventType: String, olderThan: Instant): Int {
        val result = reactiveMongoTemplate.updateMulti(
            Query(Criteria.where("event_type").`is`(eventType)
                .and("status").`is`(EventInboxStatus.PROCESSING)
                .and("last_attempted_at").lt(olderThan)),
            Update().set("status", EventInboxStatus.RECEIVED),
            EventInboxDocument::class.java,
        ).block()
        return result?.modifiedCount?.toInt() ?: 0
    }

    // ---------- PARTITION_ORDERED (partition-ordered-design.md) ----------
    // 동작·주석은 MongoEventInboxStore 와 동일 — 이 스토어는 sync 인터페이스를
    // reactive template + block() 브릿지로 구현하는 기존 스타일을 따른다.

    companion object {
        private const val KEY_LOCK_TTL_SECONDS = 60L
    }

    private val keyLockTtlIndexEnsured: Boolean by lazy {
        reactiveMongoTemplate.indexOps(EventInboxKeyLockDocument::class.java)
            .createIndex(
                Index().on("locked_at", Sort.Direction.ASC)
                    .expire(KEY_LOCK_TTL_SECONDS)
                    .named("ttl_event_inbox_key_locks_locked_at")
            )
            .block()
        true
    }

    override fun findClaimableFrontiers(eventType: String, limit: Int): List<EventInbox> {
        val blockedKeys = reactiveMongoTemplate.findDistinct(
            Query(Criteria.where("status").`in`(EventInboxStatus.PROCESSING, EventInboxStatus.FAILED)),
            "partition_key",
            EventInboxDocument::class.java,
            Long::class.java,
        ).collectList().block() ?: emptyList()

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
        return reactiveMongoTemplate.aggregate(
            aggregation,
            EventInboxDocument::class.java,
            EventInboxDocument::class.java,
        ).collectList().block() ?: emptyList()
    }

    override fun tryAcquireOrdered(eventId: Long, partitionKey: Long): Boolean {
        keyLockTtlIndexEnsured

        try {
            reactiveMongoTemplate.insert(
                EventInboxKeyLockDocument(partitionKey = partitionKey, lockedAt = Instant.now())
            ).block()
        } catch (e: DuplicateKeyException) {
            return false
        }

        try {
            val blocked = reactiveMongoTemplate.exists(
                Query(Criteria.where("partition_key").`is`(partitionKey)
                    .and("status").`in`(EventInboxStatus.PROCESSING, EventInboxStatus.FAILED)),
                EventInboxDocument::class.java,
            ).block() ?: false
            if (blocked) {
                return false
            }

            val result = reactiveMongoTemplate.findAndModify(
                Query(Criteria.where("_id").`is`(eventId).and("status").`is`(EventInboxStatus.RECEIVED)),
                Update().set("status", EventInboxStatus.PROCESSING)
                    .inc("attempt_count", 1)
                    .set("last_attempted_at", Instant.now()),
                FindAndModifyOptions.options().returnNew(false),
                EventInboxDocument::class.java,
            ).block()
            return result != null
        } finally {
            reactiveMongoTemplate.remove(
                Query(Criteria.where("_id").`is`(partitionKey)),
                EventInboxKeyLockDocument::class.java,
            ).block()
        }
    }
}
