package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.consumer

import io.github.hchanjune.omk.core.annotations.ManagedRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.entity.EventInboxEntity
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.repository.JpaEventInboxRepository
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import java.time.Instant

@ManagedRepository
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
        // event_id(TSID=시간정렬) 오름차순 — backlog 가 limit 을 넘어도 결정적으로 오래된 것부터.
        // Mongo/Reactive 스토어의 occurred_at 정렬과 동작을 통일한다.
        return repository.findAllByEventTypeAndStatus(
            eventType = eventType,
            status = status,
            pageable = PageRequest.of(0, limit, Sort.by("eventId")),
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

    override fun findClaimableFrontiers(eventType: String, limit: Int): List<EventInbox> {
        return repository.findClaimableFrontiers(eventType = eventType, limit = limit)
    }

    /**
     * advisory lock 과 게이트 CAS 가 반드시 같은 트랜잭션이어야 한다 —
     * xact lock 은 트랜잭션 종료 시 해제되고, 그 사이 게이트 검사+마킹이 원자화된다.
     * 락 획득 실패 = 타 인스턴스가 같은 키를 claim 중 → 즉시 양보(다음 폴에서 재시도).
     */
    @Transactional
    override fun tryAcquireOrdered(eventId: Long, partitionKey: Long): Boolean {
        if (!repository.tryAdvisoryXactLock(partitionKey)) {
            return false
        }
        return repository.tryAcquireOrdered(
            eventId = eventId,
            partitionKey = partitionKey,
            now = Instant.now(),
        ) > 0
    }

    @Transactional
    override fun touchProcessing(eventIds: List<Long>) {
        if (eventIds.isEmpty()) return
        repository.touchProcessing(
            eventIds = eventIds,
            processingStatus = EventInboxStatus.PROCESSING,
            now = Instant.now(),
        )
    }

    @Transactional
    override fun markReceivedForRetry(eventId: Long): Boolean {
        return repository.updateStatusIf(
            eventId = eventId,
            expectedStatus = EventInboxStatus.FAILED,
            newStatus = EventInboxStatus.RECEIVED,
        ) > 0
    }
}