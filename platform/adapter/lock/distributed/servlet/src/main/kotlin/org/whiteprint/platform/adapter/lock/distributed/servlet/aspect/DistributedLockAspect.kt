package org.whiteprint.platform.adapter.lock.distributed.servlet.aspect

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
    private val lockOperations: DistributedLockOperations
) {

    @Around("@annotation(distributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any? {
        val key = buildLockKey(joinPoint, distributedLock)
        val ttl = Duration.ofMillis(distributedLock.ttlMillis)

        val lock = acquireWithWait(key, ttl, distributedLock.waitMillis)
            ?: throw LockException(LockPolicy.ACQUISITION_FAILED, mapOf("key" to key.value))

        return try {
            joinPoint.proceed()
        } finally {
            lockOperations.releaseLock(lock)
        }
    }

    private fun buildLockKey(joinPoint: ProceedingJoinPoint, annotation: DistributedLock): LockKey {
        val method = (joinPoint.signature as MethodSignature).method
        val args = joinPoint.args
        val params = method.parameters

        val keyValues = mutableListOf<String>()

        // 파라미터에 직접 @DistributedLockKey가 붙은 경우
        params.forEachIndexed { i, param ->
            if (param.isAnnotationPresent(DistributedLockKey::class.java)) {
                args[i]?.let { keyValues.add(it.toString()) }
            }
        }

        // 파라미터 객체의 필드/프로퍼티에 @DistributedLockKey가 붙은 경우
        if (keyValues.isEmpty()) {
            args.forEach { arg ->
                if (arg == null) return@forEach
                arg::class.java.declaredFields.forEach { field ->
                    if (field.isAnnotationPresent(DistributedLockKey::class.java)) {
                        field.isAccessible = true
                        field.get(arg)?.let { keyValues.add(it.toString()) }
                    }
                }
            }
        }

        if (keyValues.isEmpty()) {
            throw LockException(
                LockPolicy.NO_LOCK_KEY_DEFINED,
                mapOf("key" to method.name)
            )
        }

        val keyPart = if (keyValues.size == 1) keyValues[0]
                      else keyValues.sorted().joinToString(":")

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
