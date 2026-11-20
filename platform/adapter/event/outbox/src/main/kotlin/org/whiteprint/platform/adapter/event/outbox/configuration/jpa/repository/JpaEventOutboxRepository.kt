package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.repository

import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.entity.EventOutboxEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStatus
import java.time.Instant

@Repository
interface JpaEventOutboxRepository: JpaRepository<EventOutboxEntity, Long> {

    @Query(
        value = """
        SELECT * FROM event_outbox
        WHERE status = 'PENDING'
        ORDER BY occurred_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
    """,
        nativeQuery = true
    )
    fun lockPending(@Param("limit") limit: Int): List<EventOutboxEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE EventOutboxEntity e
            SET e.status = :status,
                e.attemptCount = e.attemptCount + 1,
                e.lastAttemptedAt = :now
            WHERE e.eventId IN :eventIds
    """
    )
    fun bulkClaimProcess(
        @Param("eventIds") eventIds: List<Long>,
        @Param("status") status: EventOutboxStatus,
        @Param("now") now: Instant,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE EventOutboxEntity entity
    SET entity.status = :status
    WHERE entity.eventId = :eventId
""")
    fun updateStatus(
        @Param("eventId") eventId: Long,
        @Param("status") status: EventOutboxStatus,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE EventOutboxEntity e
        SET e.status = :newStatus
        WHERE e.status = :processingStatus
          AND e.lastAttemptedAt < :olderThan
    """)
    fun resetStaleProcessing(
        @Param("processingStatus") processingStatus: EventOutboxStatus,
        @Param("newStatus") newStatus: EventOutboxStatus,
        @Param("olderThan") olderThan: Instant,
    ): Int

    /** 정리 — PUBLISHED 중 오래된 행을 최대 limit 건. 인덱스 (status, occurred_at) 를 탄다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
        DELETE FROM event_outbox
        WHERE event_id IN (
            SELECT event_id FROM event_outbox
            WHERE status = 'PUBLISHED' AND occurred_at < :olderThan
            LIMIT :limit
        )
    """,
        nativeQuery = true
    )
    fun deletePublishedBefore(
        @Param("olderThan") olderThan: Instant,
        @Param("limit") limit: Int,
    ): Int

}