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
import org.whiteprint.platform.core.messaging.subscriber.DeadEventNotifier
import org.whiteprint.platform.core.messaging.subscriber.EventHandler
import org.whiteprint.platform.core.messaging.subscriber.ProcessingMode
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

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
 *   한 번에 1건씩 시간순, 다른 키는 병렬. FAILED/DEAD 는 해당 키를 블로킹한다.
 *
 * 실패 정책: 예외 발생 시 FAILED(재시도 대기) → backoff(30s→60s) 후 자동 재시도 →
 * 총 [maxAttempts]회 실행 소진 시 DEAD(종결·키 블로킹 유지) + [onEventDead] 알림.
 * DEAD 복구는 수동: status='RECEIVED', attempt_count=0 리셋.
 *
 * 생존 관리(하트비트): 처리 중인 이벤트는 30초마다 last_attempted_at 을 갱신한다.
 * stale 스캐너는 "하트비트가 staleTimeoutMillis(기본 120초) 이상 끊긴 PROCESSING"만
 * 사망으로 판정해 회수하므로, 처리 시간이 아무리 길어도 살아있는 처리를 뺏지 않는다.
 *
 * 상세: platform/adapter/event/inbox/partition-ordered-design.md
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

        /** 하트비트 주기 — staleTimeoutMillis 는 이 값의 3~4배 이상이어야 한다. */
        private const val HEARTBEAT_INTERVAL_MILLIS = 30_000L

        /** 재시도 스케줄러 폴 배치 크기. */
        private const val RETRY_BATCH_LIMIT = 100
    }

    private lateinit var inboxStore: EventInboxStore
    private lateinit var eventSerializer: InboxEventSerializer
    private lateinit var applicationContext: ApplicationContext
    private var staleTimeoutMillis: Long = 120_000L

    /** 서비스 전역 DEAD 알림 — [DeadEventNotifier] 빈이 등록돼 있으면 모든 핸들러에 공통 적용. */
    private var deadEventNotifier: DeadEventNotifier? = null

    /** 처리 모드 — 기본 SERIAL(현행 동작 그대로). 핸들러가 override 로 opt-in. */
    protected open val processingMode: ProcessingMode = ProcessingMode.SERIAL

    /**
     * PARALLEL/PARTITION_ORDERED 에서 인스턴스당 동시 처리(in-flight) 상한.
     * 각 처리 건이 DB 커넥션을 점유하므로, 적용 서비스는 커넥션 풀을
     * workerPoolSize + 여유 이상으로 상향해야 한다(HikariCP 기본 10 주의).
     */
    protected open val workerPoolSize: Int = 10

    /**
     * 총 실행 횟수 상한 — 최초 1회 + 재시도. 이 횟수째 실행이 실패하면 DEAD.
     * (attempt_count 는 claim 마다 증가하므로 크래시 후 stale 재claim 도 실행 횟수로 센다)
     */
    protected open val maxAttempts: Int = 3

    /** 재시도 backoff 기저 — n회차 실패 후 대기 = base × 2^(n-1). 기본 30s → 60s. */
    protected open val retryBackoffBaseMillis: Long = 30_000L

    /**
     * 슬롯 회계 — in-flight 를 [workerPoolSize] 로 묶는다. 실행기는 가상 스레드라 자체
     * 상한이 없고, claim 을 빈 슬롯만큼만 하기 위한 카운터로 이 세마포어를 쓴다.
     * (over-claim 금지: claim 만 하고 큐에 쌓으면 타 인스턴스 밸런싱 파괴 + stale 폭탄)
     */
    private val workerSlots: Semaphore by lazy { Semaphore(workerPoolSize) }

    private val workerPoolDelegate = lazy { Executors.newVirtualThreadPerTaskExecutor() }
    private val workerPool: ExecutorService by workerPoolDelegate

    /** 현재 이 인스턴스가 처리 중인 이벤트 id — 하트비트 대상. */
    private val inFlightEventIds = ConcurrentHashMap.newKeySet<Long>()

    /**
     * 하트비트 펌프 — Spring 스케줄러 풀과 분리된 전용 데몬 스레드.
     * (SERIAL 모드에서 폴 스레드가 긴 배치에 묶여도 하트비트는 계속 뛰어야
     * 타 인스턴스의 stale 스캐너가 살아있는 처리를 회수하지 않는다)
     */
    private val heartbeatDelegate = lazy {
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "inbox-heartbeat-${eventType}").apply { isDaemon = true }
        }.also {
            it.scheduleAtFixedRate(
                ::pumpHeartbeat,
                HEARTBEAT_INTERVAL_MILLIS,
                HEARTBEAT_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS,
            )
        }
    }
    private val heartbeat: ScheduledExecutorService by heartbeatDelegate

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        inboxStore = applicationContext.getBean<EventInboxStore>()
        eventSerializer = applicationContext.getBean<InboxEventSerializer>()
        staleTimeoutMillis = applicationContext.getBean<EventSubscriberAutoConfigurationProperties>().staleTimeoutMillis
        deadEventNotifier = applicationContext.getBeanProvider(DeadEventNotifier::class.java).getIfAvailable()
    }

    override fun destroy() {
        if (heartbeatDelegate.isInitialized()) {
            heartbeat.shutdown()
        }
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
        // 하트비트가 staleTimeoutMillis 이상 끊긴 PROCESSING = 처리 주체 사망으로 판정.
        // 살아있는 처리는 30초마다 last_attempted_at 을 갱신하므로 여기 걸리지 않는다.
        val threshold = Instant.now().minusMillis(staleTimeoutMillis)
        return inboxStore.resetStaleProcessing(eventType, threshold)
    }

    /**
     * 재시도 스케줄러 — FAILED(재시도 대기) 이벤트를 backoff 경과 후 RECEIVED 로 복귀시킨다.
     * n회차 실패 후 대기 = retryBackoffBaseMillis × 2^(n-1) (기본 30s → 60s).
     * maxAttempts 소진분은 실패 시점에 이미 DEAD 로 종결되므로 여기 잡히지 않는다(방어적 스킵만).
     */
    @Scheduled(fixedDelay = 30_000)
    @ManagedSchedule(quietWhenEmpty = true)
    open fun retryFailedEvents(): Int {
        val candidates = inboxStore.findAllByEventTypeAndStatus(
            eventType = this.eventType,
            status = EventInboxStatus.FAILED,
            limit = RETRY_BATCH_LIMIT,
        )
        if (candidates.isEmpty()) return 0

        val now = Instant.now()
        var retried = 0
        for (record in candidates) {
            if (record.attemptCount >= maxAttempts) continue // 방어 — 원래는 DEAD 로 종결됐어야 함
            val exponent = (record.attemptCount - 1).coerceAtLeast(0)
            val backoffMillis = retryBackoffBaseMillis shl exponent
            val eligibleAt = (record.lastAttemptedAt ?: record.receivedAt).plusMillis(backoffMillis)
            if (now.isBefore(eligibleAt)) continue
            if (inboxStore.markReceivedForRetry(record.eventId)) {
                retried++
            }
        }
        return retried
    }

    protected fun processOne(record: EventInbox) {
        heartbeatDelegate.value // 최초 처리 시 하트비트 펌프 기동
        inFlightEventIds.add(record.eventId)
        try {
            val event = eventSerializer.deserialize(record.payload, eventClass.java)
            applicationContext.getBean(this@AbstractEventHandler::class.java).handle(event)
            inboxStore.markCompleted(record.eventId)
        } catch (exception: Exception) {
            handleFailure(record, exception)
        } finally {
            inFlightEventIds.remove(record.eventId)
        }
    }

    private fun handleFailure(record: EventInbox, exception: Exception) {
        // record 는 claim 이전 스냅샷 — 현재 회차 = attemptCount + 1
        val attemptNumber = record.attemptCount + 1
        if (attemptNumber >= maxAttempts) {
            inboxStore.markDead(record.eventId)
            log.error(
                "Inbox event DEAD — eventType={}, eventId={}, partitionKey={}, attempts={}. " +
                    "수동 복구(status='RECEIVED', attempt_count=0) 전까지 이 키는 블로킹됩니다.",
                record.eventType, record.eventId, record.partitionKey, attemptNumber, exception,
            )
            // 서비스 전역 알림(빈 등록 시) → 핸들러별 훅 순. 각각의 예외는 본 흐름을 못 깬다.
            runCatching { deadEventNotifier?.notifyDead(record, exception) }
                .onFailure { notifierError ->
                    log.warn("DeadEventNotifier failed — eventId={}", record.eventId, notifierError)
                }
            runCatching { onEventDead(record, exception) }
                .onFailure { hookError ->
                    log.warn("onEventDead hook failed — eventId={}", record.eventId, hookError)
                }
        } else {
            inboxStore.markFailed(record.eventId, exception.message ?: "")
            log.warn(
                "Inbox event FAILED — eventType={}, eventId={}, partitionKey={}, attempt={}/{}. backoff 후 재시도됩니다.",
                record.eventType, record.eventId, record.partitionKey, attemptNumber, maxAttempts, exception,
            )
        }
    }

    private fun pumpHeartbeat() {
        val ids = inFlightEventIds.toList()
        if (ids.isEmpty()) return
        runCatching { inboxStore.touchProcessing(ids) }
            .onFailure { log.warn("Inbox heartbeat failed — eventType={}, inFlight={}", eventType, ids.size, it) }
    }

    /**
     * DEAD 발생 시 핸들러별 훅 — 재시도가 전부 소진된 종결 실패에서만 호출된다
     * (FAILED 재시도 대기 중에는 호출되지 않음). 훅 예외는 삼켜진다.
     *
     * 알림이 서비스 전역 관심사라면 이 훅 대신 [DeadEventNotifier] 빈을 하나 등록하라 —
     * 모든 핸들러의 DEAD 에 공통 적용된다. 이 훅은 특정 핸들러만의 추가 동작
     * (예: 보상 트랜잭션, 타입별 복구 로직)에 쓴다. 둘은 함께 동작한다(노티파이어 → 훅 순).
     */
    protected open fun onEventDead(record: EventInbox, exception: Exception) {}

}
