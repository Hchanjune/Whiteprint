package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.core.cache.annotation.Cached
import org.whiteprint.platform.core.cache.annotation.CachedKey
import org.whiteprint.platform.core.cache.provider.CacheProvider

@Aspect
class CachedAspect(
    private val cacheProvider: CacheProvider,
) {

    @Around("@annotation(cached)")
    fun around(joinPoint: ProceedingJoinPoint, cached: Cached): Any? =
        CacheReadThroughSupport.readThroughOrProceed(
            joinPoint = joinPoint,
            cacheProvider = cacheProvider,
            keyAnnotationClass = CachedKey::class.java,
            keyOrderOf = { it.order },
            prefix = cached.prefix,
            ttl = cached.ttl,
            timeUnit = cached.timeUnit,
        )

}
