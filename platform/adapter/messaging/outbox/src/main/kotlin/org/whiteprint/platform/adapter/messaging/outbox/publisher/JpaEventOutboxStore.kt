package org.whiteprint.platform.adapter.messaging.outbox.publisher

import org.whiteprint.platform.adapter.messaging.outbox.entity.EventOutboxEntity
import org.whiteprint.platform.adapter.messaging.outbox.repository.JpaEventOutboxRepository
import org.whiteprint.platform.core.messaging.model.event.EventStatus
import org.whiteprint.platform.core.messaging.outbox.EventOutbox
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore

class JpaEventOutboxStore(
    private val repository: JpaEventOutboxRepository
): EventOutboxStore {

    override fun save(outbox: EventOutbox): EventOutbox {
        val entity = outbox as EventOutboxEntity
        repository.save(entity)
        return entity
    }

    override fun lockPending(limit: Int): List<EventOutbox> {
        val entities = repository.lockPending(limit)
        entities.forEach {
            it.status = EventStatus.PROCESSING
        }
        return entities
    }

    override fun markPublished(eventId: Long) {
        repository.updateStatus(eventId, EventStatus.PUBLISHED)
    }

    override fun markFailed(eventId: Long) {
        repository.updateStatus(eventId, EventStatus.FAILED)
    }

}