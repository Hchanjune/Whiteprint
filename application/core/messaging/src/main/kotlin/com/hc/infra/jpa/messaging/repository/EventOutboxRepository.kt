package com.hc.infra.jpa.messaging.repository

import com.hc.core.messaging.model.EventStatus
import com.hc.core.messaging.model.EventType
import com.hc.infra.jpa.messaging.entity.EventOutboxEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventOutboxRepository: JpaRepository<EventOutboxEntity, Long> {
    fun findAllByEventName(eventName: String): List<EventOutboxEntity>
    fun findAllByEventType(eventType: EventType): List<EventOutboxEntity>
    fun findAllByStatus(status: EventStatus): List<EventOutboxEntity>
}