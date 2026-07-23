package org.whiteprint.platform.core.messaging.inbox

interface EventInboxStore {
    fun save(inbox: EventInbox): EventInbox
    fun tryAcquire(eventId: Long): Boolean
    fun markCompleted(eventId: Long)
    fun markFailed(eventId: Long, error: String)
    fun markDead(eventId: Long)
    fun findById(eventId: Long): EventInbox?
    fun findAllByEventTypeAndStatus(eventType: String, status: EventInboxStatus, limit: Int): List<EventInbox>
    fun resetStaleProcessing(eventType: String, olderThan: java.time.Instant): Int

    /**
     * 하트비트 — 처리 중(PROCESSING)인 이벤트들의 last_attempted_at 을 현재 시각으로 갱신한다.
     * 살아있는 처리자는 주기적으로 이를 호출해 stale 판정(resetStaleProcessing)에서 제외되고,
     * 죽은 처리자의 이벤트만 타임스탬프가 오래되어 회수된다. PROCESSING 이 아닌 행은 건드리지 않는다.
     */
    fun touchProcessing(eventIds: List<Long>)

    /**
     * 재시도 복귀 — FAILED 인 이벤트를 RECEIVED 로 되돌린다(CAS: FAILED 일 때만).
     * 재시도 스케줄러가 backoff 경과 후 호출한다. 성공 시 true.
     */
    fun markReceivedForRetry(eventId: Long): Boolean

    // ---------- PARTITION_ORDERED (설계: adapter/event/inbox/partition-ordered-design.md) ----------

    /**
     * claim 후보 frontier 조회 — partition_key 당 가장 이른(event_id 오름차순) RECEIVED 1건씩,
     * 이미 PROCESSING/FAILED 이벤트를 가진 키는 제외하고, 전체를 최고령 순으로 최대 [limit]건 반환한다.
     *
     * 단순 `ORDER BY … LIMIT` 은 한 키의 백로그가 조회 윈도우를 침수시켜 다른 키를
     * 기아 상태로 만들므로, 공정성(FIFO)을 위해 frontier 방식이 필수다.
     * [limit] 은 호출측의 빈 워커 슬롯 수 × 2~3배를 권장한다(claim 경쟁 패배 흡수).
     */
    fun findClaimableFrontiers(eventType: String, limit: Int): List<EventInbox>

    /**
     * 파티션 키 게이트 claim — 아래를 원자적으로 수행한다:
     * 1. [partitionKey] 의 키 락 획득(스토어별: Postgres advisory lock / Mongo claim-lock 문서).
     *    실패 시(타 인스턴스가 같은 키를 claim 중) 즉시 false.
     * 2. 같은 키에 PROCESSING/FAILED 이벤트가 없는지 확인(게이트). 있으면 false.
     * 3. [eventId] 를 RECEIVED → PROCESSING 으로 마킹(attemptCount 증가).
     *
     * 키 락은 claim 순간만 유지되며, 처리 기간의 상호배제는 PROCESSING 상태가 담당한다.
     * FAILED 가 게이트에 포함되므로 실패 이벤트는 해당 키를 블로킹한다(순서 보장).
     */
    fun tryAcquireOrdered(eventId: Long, partitionKey: Long): Boolean
}