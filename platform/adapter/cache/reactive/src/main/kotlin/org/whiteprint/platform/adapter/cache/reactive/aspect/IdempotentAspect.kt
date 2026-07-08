package org.whiteprint.platform.adapter.cache.reactive.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.whiteprint.platform.adapter.cache.reactive.support.CacheReadThroughSupport
import org.whiteprint.platform.core.cache.annotation.Idempotent
import org.whiteprint.platform.core.cache.annotation.IdempotentKey
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider

/**
 * 메커니즘은 CachedAspect와 동일하지만 의도가 다르다 — 성능 최적화가 아니라
 * "같은 요청이 재시도돼도 중복 실행/중복 응답 안 되게" 보장하는 용도.
 */
@Aspect
class IdempotentAspect(
    private val cacheProvider: ReactiveCacheProvider,
) {

    @Around("@annotation(idempotent)")
    fun around(joinPoint: ProceedingJoinPoint, idempotent: Idempotent): Any? =
        CacheReadThroughSupport.readThroughOrProceed(
            joinPoint = joinPoint,
            cacheProvider = cacheProvider,
            keyAnnotationClass = IdempotentKey::class.java,
            keyOrderOf = { it.order },
            prefix = idempotent.prefix,
            ttl = idempotent.ttl,
            timeUnit = idempotent.timeUnit,
        )

}
