package org.whiteprint.platform.adapter.lock.distributed.servlet.watchdog

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.operation.DistributedLockOperations
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 보유 중인 락을 주기적으로 연장한다(`@DistributedLock(autoExtend = true)`).
 *
 * - 락 하나당 예약 작업 하나. `ttl/3` 간격으로 `extendLock(ttl)` 을 부른다 — 한두 번 연장이 늦거나 실패해도 만료 전에 다시 기회가 있다.
 * - **상한([maxHold])** 을 넘기면 연장을 멈춘다. 멈춘 스레드가 락을 영원히 쥐지 않게 하는 장치다.
 * - 연장 결과가 false 면 **이미 락을 잃은 것**(만료 후 다른 요청이 잡음)이라 연장을 멈추고 경고를 남긴다.
 *   실행 중인 메서드를 끊지는 않는다 — 안전하게 끊을 방법이 없다. 끝나고 해제할 때도 한 번 더 경고가 난다.
 * - 연장 호출이 예외(Redis 장애 등)면 경고만 남기고 다음 주기에 다시 시도한다. 계속 실패하면 락은 ttl 로 만료되고,
 *   Redis 가 돌아온 뒤의 연장은 false 가 되어 위의 "잃음" 처리로 끝난다.
 *
 * 연장은 Redis 한 번 왕복이라 전용 스레드 둘로 충분하다. 요청 스레드와 섞지 않는 이유는, 요청 스레드가 막혀도 연장은 돌아야 해서다.
 */
class DistributedLockWatchdog(
    private val lockOperations: DistributedLockOperations,
): DisposableBean {

    private val logger = LoggerFactory.getLogger(DistributedLockWatchdog::class.java)

    private val threadCount = AtomicInteger()

    private val scheduler = ScheduledThreadPoolExecutor(THREADS) { runnable ->
        Executors.defaultThreadFactory().newThread(runnable).apply {
            name = "whiteprint-lock-watchdog-${threadCount.incrementAndGet()}"
            isDaemon = true
        }
    }.apply {
        removeOnCancelPolicy = true
    }

    /**
     * [lock] 연장을 시작한다. 반환된 [Renewal] 을 **해제 전에** 반드시 [Renewal.stop] 할 것.
     *
     * @param maxHold 연장 상한. 0 이하면 [DEFAULT_MAX_HOLD].
     */
    fun start(lock: LockHandle, ttl: Duration, maxHold: Duration): Renewal {
        val effectiveMaxHold = if (maxHold.isZero || maxHold.isNegative) DEFAULT_MAX_HOLD else maxHold
        val interval = (ttl.toMillis() / 3).coerceAtLeast(MIN_INTERVAL_MILLIS)
        val renewal = Renewal(lock, System.nanoTime() + effectiveMaxHold.toNanos(), effectiveMaxHold)

        renewal.future = scheduler.scheduleWithFixedDelay(
            { renew(renewal, ttl) },
            interval,
            interval,
            TimeUnit.MILLISECONDS,
        )
        return renewal
    }

    private fun renew(renewal: Renewal, ttl: Duration) {
        if (renewal.stopped) {
            // stop() 이 future 할당보다 먼저 불렸다면 취소가 안 됐을 수 있다 — 여기서 확실히 끊는다.
            renewal.future?.cancel(false)
            return
        }

        if (System.nanoTime() >= renewal.deadlineNanos) {
            logger.warn(
                "Distributed lock held longer than maxHold({}). Auto-extension stopped; the lock will expire within ttl({}). key={}",
                renewal.maxHold, ttl, renewal.lock.key.value,
            )
            renewal.stop()
            return
        }

        try {
            if (!lockOperations.extendLock(renewal.lock, ttl)) {
                logger.warn(
                    "Distributed lock was lost while running (expired or taken over). Auto-extension stopped. key={}, fencingToken={}",
                    renewal.lock.key.value, renewal.lock.fencingToken,
                )
                renewal.stop()
            }
        } catch (e: Exception) {
            logger.warn("Failed to extend distributed lock. Will retry on the next tick. key={}", renewal.lock.key.value, e)
        }
    }

    override fun destroy() {
        scheduler.shutdownNow()
    }

    /** 락 하나의 연장 작업. */
    class Renewal internal constructor(
        val lock: LockHandle,
        internal val deadlineNanos: Long,
        internal val maxHold: Duration,
    ) {
        @Volatile
        internal var stopped: Boolean = false

        @Volatile
        internal var future: ScheduledFuture<*>? = null

        /** 연장을 멈춘다. 여러 번 불러도 된다. */
        fun stop() {
            stopped = true
            future?.cancel(false)
        }
    }

    companion object {
        /** `@DistributedLock.maxHold` 를 지정하지 않았을 때의 연장 상한. */
        val DEFAULT_MAX_HOLD: Duration = Duration.ofMinutes(5)

        /** 아주 짧은 ttl 이어도 연장이 Redis 를 두드리지 않게 하는 최소 주기. */
        private const val MIN_INTERVAL_MILLIS = 50L

        private const val THREADS = 2
    }

}
