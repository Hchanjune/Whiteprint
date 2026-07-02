package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer

import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity.EventInboxEntity
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.repository.JpaEventInboxRepository
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import java.time.Instant

open class JpaEventInboxStore(
    private val repository: JpaEventInboxRepository
): EventInboxStore {

    @Transactional
    override fun save(inbox: EventInbox): EventInbox {
        val entity = EventInboxEntity.from(inbox)
        repository.save(entity)
        return inbox
    }

    @Transactional
    override fun tryAcquire(eventId: Long): Boolean {
        val updated = repository.tryAcquire(
            eventId = eventId,
            expectedStatus = EventInboxStatus.RECEIVED,
            newStatus = EventInboxStatus.PROCESSING,
            now = Instant.now(),
        )
        return updated > 0
    }

    @Transactional
    override fun markCompleted(eventId: Long) {
        repository.updateCompleted(
            eventId = eventId,
            status = EventInboxStatus.COMPLETED,
            now = Instant.now(),
        )
    }

    @Transactional
    override fun markFailed(eventId: Long, error: String) {
        repository.updateFailed(
            eventId = eventId,
            status = EventInboxStatus.FAILED,
            error = error,
            now = Instant.now(),
        )
    }

    @Transactional
    override fun markDead(eventId: Long) {
        repository.updateDead(
            eventId = eventId,
            status = EventInboxStatus.DEAD,
            now = Instant.now(),
        )
    }

    @Transactional
    override fun findById(eventId: Long): EventInbox? {
        return repository.findById(eventId).orElse(null)
    }

    override fun findAllByEventTypeAndStatus(
        eventType: String,
        status: EventInboxStatus,
        limit: Int
    ): List<EventInbox> {
        return repository.findAllByEventTypeAndStatus(
            eventType = eventType,
            status = status,
            pageable = PageRequest.of(0, limit),
        )
    }

    @Transactional
    override fun resetStaleProcessing(eventType: String, olderThan: Instant): Int {
        return repository.resetStaleProcessing(
            eventType = eventType,
            processingStatus = EventInboxStatus.PROCESSING,
            newStatus = EventInboxStatus.RECEIVED,
            olderThan = olderThan,
        )
    }
}