package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.consumer

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document.EventInboxDocument
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

}
