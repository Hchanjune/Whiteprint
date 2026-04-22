package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity.EventInboxEntity
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import java.time.Instant

@Repository
interface JpaEventInboxRepository: JpaRepository<EventInboxEntity, Long> {

    fun findAllByEventTypeAndStatus(
        eventType: String,
        status: EventInboxStatus,
        pageable: Pageable,
    ): List<EventInboxEntity>

    @Modifying
    @Query("""
        UPDATE EventInboxEntity e 
        SET e.status = :newStatus, 
            e.attemptCount = e.attemptCount + 1,
            e.lastAttemptedAt = :now
        WHERE e.eventId = :eventId 
          AND e.status = :expectedStatus
    """)
    fun tryAcquire(
        eventId: Long,
        expectedStatus: EventInboxStatus,
        newStatus: EventInboxStatus,
        now: Instant,
    ): Int

    @Modifying
    @Query("""
        UPDATE EventInboxEntity e 
        SET e.status = :status,
            e.processedAt = :now
        WHERE e.eventId = :eventId
    """)
    fun updateCompleted(
        eventId: Long,
        status: EventInboxStatus,
        now: Instant,
    ): Int

    @Modifying
    @Query("""
        UPDATE EventInboxEntity e 
        SET e.status = :status,
            e.errorMessage = :error,
            e.lastAttemptedAt = :now
        WHERE e.eventId = :eventId
    """)
    fun updateFailed(
        eventId: Long,
        status: EventInboxStatus,
        error: String,
        now: Instant,
    ): Int

    @Modifying
    @Query("""
        UPDATE EventInboxEntity e 
        SET e.status = :status,
            e.lastAttemptedAt = :now
        WHERE e.eventId = :eventId
    """)
    fun updateDead(
        eventId: Long,
        status: EventInboxStatus,
        now: Instant,
    ): Int

}