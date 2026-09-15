package org.whiteprint.platform.adapter.lock.distributed.servlet.aspect

import io.github.hchanjune.omk.core.metric.MetricDescriptor
import io.github.hchanjune.omk.core.metric.MetricKind
import io.github.hchanjune.omk.core.metric.MetricLayer
import io.github.hchanjune.omk.core.metric.MetricName
import io.github.hchanjune.omk.core.metric.MetricPolicy
import io.github.hchanjune.omk.core.metric.MetricTags
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import io.github.hchanjune.omk.servlet.Operations
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.whiteprint.platform.adapter.lock.distributed.servlet.watchdog.DistributedLockWatchdog
import org.whiteprint.platform.core.lock.annotation.DistributedLock
import org.whiteprint.platform.core.lock.annotation.DistributedLockKey
import org.whiteprint.platform.core.lock.context.LockContext
import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey
import org.whiteprint.platform.core.lock.operation.DistributedLockOperations
import org.whiteprint.platform.core.lock.policy.LockException
import org.whiteprint.platform.core.lock.policy.LockPolicy
import java.time.Duration

/**
 * `@DistributedLock` 메서드를 Redis 분산 락 안에서 실행한다.
 *
 * - 순서: [DistributedLockAspectOrder] — 트랜잭션 바깥, 레이트리밋 안쪽, 중복 스킵·멱등 바깥.
 * - 자동연장(기본 켜짐): 획득 직후 [DistributedLockWatchdog] 에 연장을 맡기고, **해제 전에** 멈춘다.
 * - 획득한 락을 [LockContext] 에 넣는다 — `@FencingGuarded` 엔티티 쓰기가 토큰을 읽는다. 해제 전에 뺀다.
 * - **재진입**: 같은 스레드가 같은 키를 이미 쥐고 있으면 다시 획득하지 않고 그대로 실행한다. 다시 획득하면 자기 자신이
 *   놓기를 `wait` 만큼 기다리다 `LOCK_ACQUISITION_FAILED` 가 난다. 안쪽 호출의 `ttl`·`wait`·`autoExtend`·`maxHold` 는
 *   무시되고 바깥 락의 설정이 그대로 적용된다(연장·해제도 바깥이 한다). 판정은 스레드 로컬이라, 락 구간에서 다른 스레드로
 *   넘긴 작업이 같은 키를 잡으려 하면 재진입이 아니라 일반 대기가 된다.
 * - **해제 실패는 메서드 결과를 바꾸지 않는다.** 해제는 `finally` 에서 돌기 때문에 여기서 예외를 던지면
 *   메서드의 반환값이나 원래 예외가 사라진다. 실패는 경고 로그로만 남기고, 락은 TTL 로 풀린다.
 */
@Aspect
@Order(DistributedLockAspectOrder.DISTRIBUTED_LOCK)
class DistributedLockAspect(
    private val lockOperations: DistributedLockOperations,
    private val spanIdProvider: SpanIdProvider,
    private val watchdog: DistributedLockWatchdog,
) {

    @Around("@annotation(distributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any? {
        val key = buildLockKey(joinPoint, distributedLock)
        if (LockContext.isHeld(key)) return joinPoint.proceed()

        val ttl = Duration.ofMillis(distributedLock.timeUnit.toMillis(distributedLock.ttl))
        val waitMillis = distributedLock.timeUnit.toMillis(distributedLock.wait)

        val lock = acquireWithSpan(key, ttl, waitMillis)
            ?: throw LockException(LockPolicy.ACQUISITION_FAILED, mapOf("key" to key.value))

        val renewal = if (distributedLock.autoExtend) {
            watchdog.start(
                lock = lock,
                ttl = ttl,
                maxHold = Duration.ofMillis(distributedLock.timeUnit.toMillis(distributedLock.maxHold)),
            )
        } else {
            null
        }

        LockContext.push(lock)
        return try {
            joinPoint.proceed()
        } finally {
            LockContext.pop(lock)
            // 해제보다 먼저 멈춘다 — 해제 뒤에 연장이 돌면 owner 가 달라 실패할 뿐이지만, 불필요한 경고가 남는다.
            renewal?.stop()
            releaseWithSpan(lock, key)
        }
    }

    private fun acquireWithSpan(key: LockKey, ttl: Duration, waitMillis: Long): LockHandle? {
        if (!Operations.hasContext) return acquireWithWait(key, ttl, waitMillis)

        val context = Operations.context
        val span = context.push(
            name = MetricName("lock.acquire"),
            kind = MetricKind.TIMER,
            policy = MetricPolicy.defaults(),
            tags = buildTags(key, context.operation),
            descriptor = MetricDescriptor(
                operation = context.operation,
                useCase = context.useCase,
                layer = MetricLayer.EXTERNAL,
            ),
            idProvider = spanIdProvider,
        )

        return try {
            val lock = acquireWithWait(key, ttl, waitMillis)
            span.end()
            context.pop()
            lock
        } catch (e: Throwable) {
            span.end(e)
            context.pop()
            throw e
        }
    }

    private val logger = LoggerFactory.getLogger(DistributedLockAspect::class.java)

    private fun releaseWithSpan(lock: LockHandle, key: LockKey) {
        if (!Operations.hasContext) {
            runCatching { releaseOrWarn(lock) }
                .onFailure { logReleaseFailure(key, it) }
            return
        }

        val context = Operations.context
        val span = context.push(
            name = MetricName("lock.release"),
            kind = MetricKind.TIMER,
            policy = MetricPolicy.defaults(),
            tags = buildTags(key, context.operation),
            descriptor = MetricDescriptor(
                operation = context.operation,
                useCase = context.useCase,
                layer = MetricLayer.EXTERNAL,
            ),
            idProvider = spanIdProvider,
        )

        try {
            releaseOrWarn(lock)
            span.end()
            context.pop()
        } catch (e: Throwable) {
            span.end(e)
            context.pop()
            logReleaseFailure(key, e)
        }
    }

    /**
     * 해제했는데 지운 게 없으면 **이미 락을 잃은 것**이다(TTL 만료 후 다른 요청이 잡았거나 그냥 만료됐다).
     * 보호 구간이 락 없이 끝까지 돌았다는 뜻이라 경고로 남긴다 — TTL 이 처리 시간보다 짧다는 신호다.
     */
    private fun releaseOrWarn(lock: LockHandle) {
        if (!lockOperations.releaseLock(lock)) {
            logger.warn(
                "Distributed lock was already lost before release (expired or taken over). key={}, fencingToken={}. " +
                    "The protected section may have run without the lock — ttl is likely shorter than the execution time.",
                lock.key.value, lock.fencingToken,
            )
        }
    }

    private fun logReleaseFailure(key: LockKey, e: Throwable) {
        logger.warn("Failed to release distributed lock. It will be released by ttl. key={}", key.value, e)
    }

    private fun buildTags(key: LockKey, operation: String): MetricTags =
        MetricTags.Builder()
            .put("lock_key", key.value)
            .put("operation", operation)
            .build()

    private fun buildLockKey(joinPoint: ProceedingJoinPoint, annotation: DistributedLock): LockKey {
        val method = (joinPoint.signature as MethodSignature).method
        val args = joinPoint.args
        val params = method.parameters

        val keyEntries = mutableListOf<Pair<Int, String>>()

        params.forEachIndexed { i, param ->
            param.getAnnotation(DistributedLockKey::class.java)?.let { keyAnnotation ->
                args[i]?.let { keyEntries.add(keyAnnotation.order to it.toString()) }
            }
        }

        if (keyEntries.isEmpty()) {
            args.forEach { arg ->
                if (arg == null) return@forEach
                arg::class.java.declaredFields.forEach { field ->
                    field.getAnnotation(DistributedLockKey::class.java)?.let { keyAnnotation ->
                        field.isAccessible = true
                        field.get(arg)?.let { keyEntries.add(keyAnnotation.order to it.toString()) }
                    }
                }
            }
        }

        if (keyEntries.isEmpty()) {
            throw LockException(LockPolicy.NO_LOCK_KEY_DEFINED, mapOf("key" to method.name))
        }

        // order가 같으면(기본값 0 포함) 선언 순서를 유지한다 (stable sort).
        val keyPart = if (keyEntries.size == 1) keyEntries[0].second
                      else keyEntries.sortedBy { it.first }.joinToString(":") { it.second }

        val prefix = annotation.prefix
        return if (prefix.isBlank()) LockKey(keyPart) else LockKey("$prefix:$keyPart")
    }

    private fun acquireWithWait(key: LockKey, ttl: Duration, waitMillis: Long): LockHandle? {
        val deadline = System.currentTimeMillis() + waitMillis
        val retryInterval = 50L

        while (System.currentTimeMillis() < deadline) {
            val lock = lockOperations.acquireLock(key, ttl)
            if (lock != null) return lock
            val remaining = deadline - System.currentTimeMillis()
            if (remaining > 0) Thread.sleep(retryInterval.coerceAtMost(remaining))
        }
        return lockOperations.acquireLock(key, ttl)
    }

}
