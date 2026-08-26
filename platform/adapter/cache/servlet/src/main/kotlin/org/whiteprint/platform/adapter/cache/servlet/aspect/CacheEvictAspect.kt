package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.core.cache.annotation.CacheEvict
import org.whiteprint.platform.core.cache.annotation.CacheEvictKey
import org.whiteprint.platform.core.cache.provider.CacheProvider

@Aspect
class CacheEvictAspect(
    private val cacheProvider: CacheProvider,
) {

    @Around("@annotation(cacheEvict)")
    fun around(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? {
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
