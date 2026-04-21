package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer

import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity.EventInboxEntity
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.repository.JpaEventInboxRepository
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore

open class JpaEventInboxStore(
    private val repository: JpaEventInboxRepository
): EventInboxStore {

    @Transactional
    override fun save(inbox: EventInbox): EventInbox {
        val entity = EventInboxEntity.from(inbox)
        repository.save(entity)
        return inbox
    }

    override fun tryAcquire(eventId: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun markCompleted(eventId: Long) {
        TODO("Not yet implemented")
    }

    override fun markFailed(eventId: Long, error: String) {
        TODO("Not yet implemented")
    }

    override fun markDead(eventId: Long) {
        TODO("Not yet implemented")
    }

    override fun findById(eventId: Long): EventInbox? {
        TODO("Not yet implemented")
    }

    override fun findAllByEventTypeAndStatus(
        eventType: String,
        status: EventInboxStatus,
        limit: Int
    ): List<EventInbox> {
        TODO("Not yet implemented")
    }
}