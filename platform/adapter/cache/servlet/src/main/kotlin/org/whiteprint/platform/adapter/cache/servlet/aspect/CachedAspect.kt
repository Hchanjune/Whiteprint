package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.whiteprint.platform.adapter.cache.common.aspect.CacheAspectOrder
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.servlet.support.CacheSpanSupport
import org.whiteprint.platform.core.cache.annotation.Cached
import org.whiteprint.platform.core.cache.annotation.CachedKey
import org.whiteprint.platform.core.cache.provider.CacheProvider

@Aspect
@Order(CacheAspectOrder.CACHED)
class CachedAspect(
    private val cacheProvider: CacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(cached)")
    fun around(joinPoint: ProceedingJoinPoint, cached: Cached): Any? =
        CacheSpanSupport.around(joinPoint, spanIdProvider) { proceed(joinPoint, cached) }

    private fun proceed(joinPoint: ProceedingJoinPoint, cached: Cached): Any? =
        CacheReadThroughSupport.readThroughOrProceed(
            joinPoint = joinPoint,
            cacheProvider = cacheProvider,
            keyAnnotationClass = CachedKey::class.java,
            keyNameOf = { it.name },
            prefix = cached.prefix,
            ttl = cached.ttl,
            timeUnit = cached.timeUnit,
        )

}
