package org.whiteprint.platform.adapter.event.outbox.configuration.mongo.producer

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.whiteprint.platform.adapter.event.outbox.configuration.mongo.document.EventOutboxDocument
import org.whiteprint.platform.core.messaging.outbox.EventOutbox
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStatus
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import java.time.Instant

@ManagedRepository
open class ReactiveMongoEventOutboxStore(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : EventOutboxStore {

    override fun save(outbox: EventOutbox): EventOutbox {
        reactiveMongoTemplate.save(EventOutboxDocument.from(outbox)).block()
        return outbox
    }

    override fun claimPending(limit: Int): List<EventOutbox> {
        val findQuery = Query(Criteria.where("status").`is`(EventOutboxStatus.PENDING))
            .with(Sort.by("occurred_at").ascending())
            .limit(limit)
        val pending = reactiveMongoTemplate.find(findQuery, EventOutboxDocument::class.java)
            .collectList().block() ?: return emptyList()
        if (pending.isEmpty()) return emptyList()

        val ids = pending.map { it.eventId }
        val claimQuery = Query(
            Criteria.where("_id").`in`(ids).and("status").`is`(EventOutboxStatus.PENDING)
        )
        reactiveMongoTemplate.updateMulti(
            claimQuery,
            Update().set("status", EventOutboxStatus.PROCESSING)
                .inc("attempt_count", 1)
                .set("last_attempted_at", Instant.now()),
            EventOutboxDocument::class.java,
        ).block()
        return pending
    }

    override fun markPublished(eventId: Long) = updateStatus(eventId, EventOutboxStatus.PUBLISHED)

    override fun markFailed(eventId: Long) = updateStatus(eventId, EventOutboxStatus.FAILED)

    override fun resetStaleProcessing(olderThan: Instant): Int {
        val result = reactiveMongoTemplate.updateMulti(
            Query(Criteria.where("status").`is`(EventOutboxStatus.PROCESSING)
                .and("last_attempted_at").lt(olderThan)),
            Update().set("status", EventOutboxStatus.PENDING),
            EventOutboxDocument::class.java,
        ).block()
        return result?.modifiedCount?.toInt() ?: 0
    }

    private fun updateStatus(eventId: Long, status: EventOutboxStatus) {
        reactiveMongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", status),
            EventOutboxDocument::class.java,
        ).block()
    }
}
