package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.whiteprint.platform.adapter.cache.common.aspect.CacheAspectOrder
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.servlet.support.CacheSpanSupport
import org.whiteprint.platform.core.cache.annotation.CacheEvict
import org.whiteprint.platform.core.cache.annotation.CacheEvictKey
import org.whiteprint.platform.core.cache.provider.CacheProvider

@Aspect
@Order(CacheAspectOrder.CACHE_EVICT)
class CacheEvictAspect(
    private val cacheProvider: CacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(cacheEvict)")
    fun around(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? =
        CacheSpanSupport.around(joinPoint, spanIdProvider) { proceed(joinPoint, cacheEvict) }

    private fun proceed(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = CacheEvictKey::class.java,
            keyNameOf = { it.name },
            prefix = cacheEvict.prefix,
        )

        if (cacheEvict.beforeInvocation) {
            cacheProvider.value.delete(key)
            return joinPoint.proceed()
        }

        val result = joinPoint.proceed()
        cacheProvider.value.delete(key)
        return result
    }

}
