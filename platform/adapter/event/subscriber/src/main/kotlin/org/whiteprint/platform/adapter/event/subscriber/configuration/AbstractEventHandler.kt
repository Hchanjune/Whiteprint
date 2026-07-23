package org.whiteprint.platform.adapter.event.subscriber.configuration

import io.github.hchanjune.omk.core.annotations.ManagedSchedule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.annotation.Scheduled
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStatus
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.subscriber.EventHandler
import org.whiteprint.platform.core.messaging.subscriber.ProcessingMode
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

/**
 * Abstract base for transactional inbox event handlers.
 *
 * Polls the inbox store on a fixed schedule, acquires per-record locks to prevent
 * duplicate processing, and delegates to [handle] with the deserialized payload.
 * Subclasses only need to implement [handle].
 *
 * 처리 모드([processingMode], 기본 SERIAL=현행 동작):
 * - SERIAL: 폴 스레드에서 배치 직렬 인라인 처리(하위호환).
 * - PARALLEL: event 단위 claim + 워커풀 병렬(순서 무보장 — 멱등 핸들러 전용).
 * - PARTITION_ORDERED: partition_key 게이트 claim — 같은 키는 전체 인스턴스를 통틀어
 *   한 번에 1건씩 시간순, 다른 키는 병렬. FAILED 는 해당 키를 블로킹한다.
 *   상세: platform/adapter/event/inbox/partition-ordered-design.md
 *
 * Observability context (traceId, causationId, etc.) is handled by the
 * [@ManagedEventHandler][io.github.hchanjune.omk.core.annotations.ManagedEventHandler]
 * AOP aspect — annotate [handle] and declare the relevant fields on your event class.
 */
abstract class AbstractEventHandler<E: Event>: EventHandler<E>, ApplicationContextAware, DisposableBean {

    companion object {
        private val log = LoggerFactory.getLogger(AbstractEventHandler::class.java)

        /** SERIAL 모드 폴 배치 크기(현행 유지). */
        private const val SERIAL_BATCH_LIMIT = 100

        /** 병렬 모드 조회 배수 — 빈 슬롯 × 3. 멀티 인스턴스 claim 경쟁 패배 흡수용. */
        private const val FETCH_MULTIPLIER = 3
    }

    private lateinit var inboxStore: EventInboxStore
    private lateinit var eventSerializer: InboxEventSerializer
    private lateinit var applicationContext: ApplicationContext
    private var claimTimeoutMillis: Long = 300_000L

    /** 처리 모드 — 기본 SERIAL(현행 동작 그대로). 핸들러가 override 로 opt-in. */
    protected open val processingMode: ProcessingMode = ProcessingMode.SERIAL

    /**
     * PARALLEL/PARTITION_ORDERED 에서 인스턴스당 동시 처리(in-flight) 상한.
     * 각 처리 건이 DB 커넥션을 점유하므로, 적용 서비스는 커넥션 풀을
     * workerPoolSize + 여유 이상으로 상향해야 한다(HikariCP 기본 10 주의).
     */
    protected open val workerPoolSize: Int = 10

    /**
     * 슬롯 회계 — in-flight 를 [workerPoolSize] 로 묶는다. 실행기는 가상 스레드라 자체
     * 상한이 없고, claim 을 빈 슬롯만큼만 하기 위한 카운터로 이 세마포어를 쓴다.
     * (over-claim 금지: claim 만 하고 큐에 쌓으면 타 인스턴스 밸런싱 파괴 + stale 폭탄)
     */
    private val workerSlots: Semaphore by lazy { Semaphore(workerPoolSize) }

    private val workerPoolDelegate = lazy { Executors.newVirtualThreadPerTaskExecutor() }
    private val workerPool: ExecutorService by workerPoolDelegate

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        inboxStore = applicationContext.getBean<EventInboxStore>()
        eventSerializer = applicationContext.getBean<InboxEventSerializer>()
        claimTimeoutMillis = applicationContext.getBean<EventSubscriberAutoConfigurationProperties>().claimTimeoutMillis
    }

    override fun destroy() {
        if (workerPoolDelegate.isInitialized()) {
            workerPool.shutdown()
        }
    }

    // Intentionally NOT @ManagedSchedule: each record's handle() opens its own context via
    // @ManagedEventHandler and extracts the event's original traceId. A schedule-owned
    // context here would make handle() a non-owner and skip that per-event extraction,
    // collapsing every record in the batch into the poller's trace.
    @Scheduled(fixedDelay = 500)
    open fun pollAndProcess() {
        when (processingMode) {
            ProcessingMode.SERIAL -> pollSerial()
            ProcessingMode.PARALLEL -> pollParallel()
            ProcessingMode.PARTITION_ORDERED -> pollPartitionOrdered()
        }
    }

    /** 현행 동작 그대로 — 폴 스레드에서 직렬 인라인 처리. */
    private fun pollSerial() {
        val records = inboxStore.findAllByEventTypeAndStatus(
            eventType = this.eventType,
            status = EventInboxStatus.RECEIVED,
            limit = SERIAL_BATCH_LIMIT,
        )

        records.forEach { record ->
            if (!inboxStore.tryAcquire(record.eventId)) {
                return@forEach
            }
            processOne(record)
        }
    }

    /** event 단위 claim + 워커풀 병렬. 순서 무보장. */
    private fun pollParallel() {
        val freeSlots = workerSlots.availablePermits()
        if (freeSlots <= 0) return

        val records = inboxStore.findAllByEventTypeAndStatus(
            eventType = this.eventType,
            status = EventInboxStatus.RECEIVED,
            limit = freeSlots * FETCH_MULTIPLIER,
        )

        for (record in records) {
            if (!workerSlots.tryAcquire()) break
            if (!inboxStore.tryAcquire(record.eventId)) {
                workerSlots.release()
                continue
            }
            dispatch(record)
        }
    }

    /**
     * partition_key 게이트 claim + 워커풀 병렬.
     * frontier 조회(키당 최선두 1건, 블로킹 키 제외, 최고령 순)로 공정성을 확보하고,
     * claim 은 빈 슬롯만큼만 — 폴 스레드는 절대 블로킹하지 않는다.
     */
    private fun pollPartitionOrdered() {
        val freeSlots = workerSlots.availablePermits()
        if (freeSlots <= 0) return

        val frontiers = inboxStore.findClaimableFrontiers(
            eventType = this.eventType,
            limit = freeSlots * FETCH_MULTIPLIER,
        )

        for (record in frontiers) {
            if (!workerSlots.tryAcquire()) break
            if (!inboxStore.tryAcquireOrdered(record.eventId, record.partitionKey)) {
                // 키 락 경쟁 패배 또는 게이트 불통과 — 다음 폴에서 재시도된다.
                workerSlots.release()
                continue
            }
            dispatch(record)
        }
    }

    private fun dispatch(record: EventInbox) {
        workerPool.execute {
            try {
                processOne(record)
            } finally {
                workerSlots.release()
            }
        }
    }

    @Scheduled(fixedDelay = 60_000)
    @ManagedSchedule(quietWhenEmpty = true)
    open fun recoverStaleProcessing(): Int {
        // PARTITION_ORDERED 에선 방치된 PROCESSING 이 해당 키 전체를 블로킹하므로,
        // claimTimeout 이 곧 "인스턴스 사망 시 키 블로킹 지속 시간"이 된다.
        val threshold = Instant.now().minusMillis(claimTimeoutMillis)
        return inboxStore.resetStaleProcessing(eventType, threshold)
    }

    protected fun processOne(record: EventInbox) {
        try {
            val event = eventSerializer.deserialize(record.payload, eventClass.java)
            applicationContext.getBean(this@AbstractEventHandler::class.java).handle(event)
            inboxStore.markCompleted(record.eventId)
        } catch (exception: Exception) {
            inboxStore.markFailed(record.eventId, exception.message ?: "")
            if (processingMode == ProcessingMode.PARTITION_ORDERED) {
                // FAILED 는 키 블로킹을 의미 — 복구(RECEIVED 리셋) 전까지 이 키의 후속 이벤트가 정지한다.
                log.error(
                    "Inbox event FAILED — eventType={}, eventId={}, partitionKey={} 는 복구 전까지 블로킹됩니다.",
                    record.eventType, record.eventId, record.partitionKey, exception,
                )
            }
            runCatching { onEventFailed(record, exception) }
                .onFailure { hookError ->
                    log.warn("onEventFailed hook failed — eventId={}", record.eventId, hookError)
                }
        }
    }

    /**
     * FAILED 발생 훅 — 알림 연동 확장점.
     * PARTITION_ORDERED 에선 FAILED 가 해당 partition_key 를 블로킹하므로
     * 운영 알림(슬랙/메트릭 등)을 이 훅에서 연결할 것. 훅 예외는 삼켜진다.
     */
    protected open fun onEventFailed(record: EventInbox, exception: Exception) {}

}
