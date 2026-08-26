package org.whiteprint.platform.adapter.cache.reactive.aspect

import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.whiteprint.platform.adapter.cache.reactive.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.reactive.support.asMono
import org.whiteprint.platform.core.cache.annotation.CacheEvict
import org.whiteprint.platform.core.cache.annotation.CacheEvictKey
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider

@Aspect
class CacheEvictAspect(
    private val cacheProvider: ReactiveCacheProvider,
) {

    @Around("@annotation(cacheEvict)")
    fun around(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = CacheEvictKey::class.java,
            keyNameOf = { it.name },
            prefix = cacheEvict.prefix,
        )

        return if (cacheEvict.beforeInvocation) {
            mono { cacheProvider.value.delete(key) }.flatMap { asMono(joinPoint.proceed()) }
        } else {
            asMono(joinPoint.proceed()).flatMap { result ->
                mono { cacheProvider.value.delete(key) }.thenReturn(result)
            }
        }
    }

}
