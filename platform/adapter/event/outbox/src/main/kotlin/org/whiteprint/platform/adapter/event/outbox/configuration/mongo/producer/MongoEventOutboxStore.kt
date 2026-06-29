package org.whiteprint.platform.adapter.event.outbox.configuration.mongo.producer

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.whiteprint.platform.adapter.event.outbox.configuration.mongo.document.EventOutboxDocument
import org.whiteprint.platform.adapter.event.outbox.configuration.mongo.repository.MongoEventOutboxRepository
import org.whiteprint.platform.core.messaging.outbox.EventOutbox
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStatus
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import java.time.Instant

class MongoEventOutboxStore(
    private val repository: MongoEventOutboxRepository,
    private val mongoTemplate: MongoTemplate,
) : EventOutboxStore {

    override fun save(outbox: EventOutbox): EventOutbox {
        repository.save(EventOutboxDocument.from(outbox))
        return outbox
    }

    override fun claimPending(limit: Int): List<EventOutbox> {
        val findQuery = Query(Criteria.where("status").`is`(EventOutboxStatus.PENDING))
            .with(Sort.by("occurred_at").ascending())
            .limit(limit)
        val pending = mongoTemplate.find(findQuery, EventOutboxDocument::class.java)
        if (pending.isEmpty()) return emptyList()

        val ids = pending.map { it.eventId }
        val now = Instant.now()
        val claimQuery = Query(
            Criteria.where("_id").`in`(ids).and("status").`is`(EventOutboxStatus.PENDING)
        )
        mongoTemplate.updateMulti(
            claimQuery,
            Update().set("status", EventOutboxStatus.PROCESSING)
                .inc("attempt_count", 1)
                .set("last_attempted_at", now),
            EventOutboxDocument::class.java,
        )
        return pending
    }

    override fun markPublished(eventId: Long) = updateStatus(eventId, EventOutboxStatus.PUBLISHED)

    override fun markFailed(eventId: Long) = updateStatus(eventId, EventOutboxStatus.FAILED)

    private fun updateStatus(eventId: Long, status: EventOutboxStatus) {
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(eventId)),
            Update().set("status", status),
            EventOutboxDocument::class.java,
        )
    }

}
