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

    fun findAllByTraceId(traceId: String): List<EventInboxEntity>

    fun findAllByCausationId(causationId: String): List<EventInboxEntity>

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

    @Modifying
    @Query("""
        UPDATE EventInboxEntity e
        SET e.status = :newStatus
        WHERE e.eventType = :eventType
          AND e.status = :processingStatus
          AND e.lastAttemptedAt < :olderThan
    """)
    fun resetStaleProcessing(
        eventType: String,
        processingStatus: EventInboxStatus,
        newStatus: EventInboxStatus,
        olderThan: Instant,
    ): Int

    // ---------- PARTITION_ORDERED (Postgres 전용, partition-ordered-design.md) ----------

    /**
     * 파티션 키 advisory lock (트랜잭션 스코프 — 커밋/롤백 시 자동 해제).
     * claim 단계의 키 단위 상호배제 전용: 같은 키의 서로 다른 두 이벤트를
     * 두 인스턴스가 동시에 claim 하는 레이스를 막는다.
     */
    @Query(value = "SELECT pg_try_advisory_xact_lock(:partitionKey)", nativeQuery = true)
    fun tryAdvisoryXactLock(partitionKey: Long): Boolean

    /**
     * 키 게이트 claim — 같은 키에 PROCESSING/FAILED 가 없을 때만 RECEIVED → PROCESSING.
     * FAILED 포함이 순서 보장의 핵심: 실패 이벤트가 키를 블로킹한다.
     * 반드시 [tryAdvisoryXactLock] 획득 후 같은 트랜잭션에서 호출할 것.
     */
    @Modifying
    @Query(value = """
        UPDATE event_inbox
        SET status = 'PROCESSING',
            attempt_count = attempt_count + 1,
            last_attempted_at = :now
        WHERE event_id = :eventId
          AND status = 'RECEIVED'
          AND NOT EXISTS (
              SELECT 1 FROM event_inbox b
              WHERE b.partition_key = :partitionKey
                AND b.status IN ('PROCESSING', 'FAILED')
          )
    """, nativeQuery = true)
    fun tryAcquireOrdered(
        eventId: Long,
        partitionKey: Long,
        now: Instant,
    ): Int

    /**
     * claim 후보 frontier — 키당 최선두(event_id 최소) RECEIVED 1건씩,
     * PROCESSING/FAILED 를 가진 키는 제외, 최고령 순으로 최대 :limit 건.
     * DISTINCT ON 으로 키당 1건만 뽑아 한 키의 백로그가 조회 윈도우를
     * 침수시키는 기아를 방지한다(공정성 필수 요건).
     */
    @Query(value = """
        SELECT f.* FROM (
            SELECT DISTINCT ON (partition_key) *
            FROM event_inbox
            WHERE event_type = :eventType
              AND status = 'RECEIVED'
            ORDER BY partition_key, event_id
        ) f
        WHERE NOT EXISTS (
            SELECT 1 FROM event_inbox b
            WHERE b.partition_key = f.partition_key
              AND b.status IN ('PROCESSING', 'FAILED')
        )
        ORDER BY f.event_id
        LIMIT :limit
    """, nativeQuery = true)
    fun findClaimableFrontiers(
        eventType: String,
        limit: Int,
    ): List<EventInboxEntity>

}