package org.whiteprint.platform.core.messaging.outbox

interface EventOutboxStore {

    fun save(outbox: EventOutbox): EventOutbox

    fun claimPending(limit: Int): List<EventOutbox>

    fun markPublished(eventId: Long)

    fun markFailed(eventId: Long)

    fun resetStaleProcessing(olderThan: java.time.Instant): Int

    /**
     * 정리(purge) — PUBLISHED 이고 occurred_at 이 [olderThan] 보다 이른 행을 최대 [limit] 건 지운다. 지운 건수를 돌려준다.
     * PENDING·PROCESSING·FAILED 는 건드리지 않는다(발행 실패는 조사 근거).
     * 호출측(정리 스케줄러)이 반환값이 [limit] 이면 빌 때까지 반복한다 — 한 번에 대량 삭제하지 않기 위해서다.
     *
     * 기본 구현은 미지원 예외 — 플랫폼 스토어(JPA·Mongo·Reactive Mongo)는 모두 구현한다.
     */
    fun purgePublished(olderThan: java.time.Instant, limit: Int): Int =
        throw UnsupportedOperationException("${this::class.simpleName} does not support outbox purge.")

}