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
import org.whiteprint.platform.core.lock.annotation.DistributedLock
import org.whiteprint.platform.core.lock.annotation.DistributedLockKey
import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey
import org.whiteprint.platform.core.lock.operation.DistributedLockOperations
import org.whiteprint.platform.core.lock.policy.LockException
import org.whiteprint.platform.core.lock.policy.LockPolicy
import java.time.Duration

@Aspect
class DistributedLockAspect(
    private val lockOperations: DistributedLockOperations,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(distributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any? {
        val key = buildLockKey(joinPoint, distributedLock)
        val ttl = Duration.ofMillis(distributedLock.timeUnit.toMillis(distributedLock.ttl))
        val waitMillis = distributedLock.timeUnit.toMillis(distributedLock.wait)

        val lock = acquireWithSpan(key, ttl, waitMillis)
            ?: throw LockException(LockPolicy.ACQUISITION_FAILED, mapOf("key" to key.value))

        return try {
            joinPoint.proceed()
        } finally {
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

    private fun releaseWithSpan(lock: LockHandle, key: LockKey) {
        if (!Operations.hasContext) {
            lockOperations.releaseLock(lock)
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
            lockOperations.releaseLock(lock)
            span.end()
            context.pop()
        } catch (e: Throwable) {
            span.end(e)
            context.pop()
        }
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
