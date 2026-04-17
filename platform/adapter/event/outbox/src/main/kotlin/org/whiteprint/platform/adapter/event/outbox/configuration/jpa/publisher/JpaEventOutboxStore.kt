package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.publisher

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.entity.EventOutboxEntity
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.repository.JpaEventOutboxRepository
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStatus
import org.whiteprint.platform.core.messaging.outbox.EventOutbox
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import java.time.Instant

open class JpaEventOutboxStore(
    private val repository: JpaEventOutboxRepository
): EventOutboxStore {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(outbox: EventOutbox): EventOutbox {
        val entity = EventOutboxEntity.from(outbox)
        repository.save(entity)
        return outbox
    }

    @Transactional
    override fun claimPending(limit: Int): List<EventOutbox> {
        val entities = repository.lockPending(limit)
        if (entities.isEmpty()) return emptyList()

        val ids = entities.map { it.eventId }
        val now = Instant.now()
        repository.bulkClaimProcess(ids, EventOutboxStatus.PROCESSING, now)
        return entities
    }

    @Transactional
    override fun markPublished(eventId: Long) {
        repository.updateStatus(eventId, EventOutboxStatus.PUBLISHED)
    }

    @Transactional
    override fun markFailed(eventId: Long) {
        repository.updateStatus(eventId, EventOutboxStatus.FAILED)
    }

}