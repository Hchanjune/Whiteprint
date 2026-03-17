package org.whiteprint.platform.adapter.messaging.repository

import org.whiteprint.platform.adapter.messaging.entity.EventOutboxEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.whiteprint.platform.core.messaging.model.EventStatus
import org.whiteprint.platform.core.messaging.model.EventType

@Repository
interface EventOutboxRepository: JpaRepository<EventOutboxEntity, Long> {
    fun findAllByEventName(eventName: String): List<EventOutboxEntity>
    fun findAllByEventType(eventType: EventType): List<EventOutboxEntity>
    fun findAllByStatus(status: EventStatus): List<EventOutboxEntity>
}