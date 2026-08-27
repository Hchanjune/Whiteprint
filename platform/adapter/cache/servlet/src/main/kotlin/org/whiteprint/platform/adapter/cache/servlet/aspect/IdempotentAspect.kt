package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.whiteprint.platform.adapter.cache.common.aspect.CacheAspectOrder
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.servlet.support.CacheSpanSupport
import org.whiteprint.platform.core.cache.annotation.Idempotent
import org.whiteprint.platform.core.cache.annotation.IdempotentKey
import org.whiteprint.platform.core.cache.provider.CacheProvider

/**
 * 메커니즘은 CachedAspect와 동일(캐시 hit면 그 값 반환, 없으면 실행 후 저장)하지만
 * 의도가 다르다 — 성능 최적화가 아니라 "같은 요청이 재시도돼도 중복 실행/중복 응답 안 되게" 보장하는 용도.
 */
@Aspect
@Order(CacheAspectOrder.IDEMPOTENT)
class IdempotentAspect(
    private val cacheProvider: CacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(idempotent)")
    fun around(joinPoint: ProceedingJoinPoint, idempotent: Idempotent): Any? =
        CacheSpanSupport.around(joinPoint, spanIdProvider) { proceed(joinPoint, idempotent) }

    private fun proceed(joinPoint: ProceedingJoinPoint, idempotent: Idempotent): Any? =
        CacheReadThroughSupport.readThroughOrProceed(
            joinPoint = joinPoint,
            cacheProvider = cacheProvider,
            keyAnnotationClass = IdempotentKey::class.java,
            keyNameOf = { it.name },
            prefix = idempotent.prefix,
            ttl = idempotent.ttl,
            timeUnit = idempotent.timeUnit,
        )

}
