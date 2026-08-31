package org.whiteprint.platform.adapter.cache.reactive.aspect

import org.whiteprint.platform.adapter.cache.common.aspect.CacheAspectOrder
import kotlinx.coroutines.reactor.mono
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.whiteprint.platform.adapter.cache.reactive.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.reactive.support.CacheSpanSupport
import org.whiteprint.platform.adapter.cache.reactive.support.proceedCold
import org.whiteprint.platform.core.cache.annotation.CacheEvict
import org.whiteprint.platform.core.cache.annotation.CacheEvictKey
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider

@Aspect
@Order(CacheAspectOrder.CACHE_EVICT)
class CacheEvictAspect(
    private val cacheProvider: ReactiveCacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(cacheEvict)")
    fun around(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? =
        CacheSpanSupport.aroundMono(joinPoint, spanIdProvider) { proceed(joinPoint, cacheEvict) }

    private fun proceed(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = CacheEvictKey::class.java,
            keyNameOf = { it.name },
            prefix = cacheEvict.prefix,
        )

        // proceed() 는 flatMap 안이 아니라 여기서 불러야 한다 — 이유는 proceedCold 참조.
        val target = proceedCold(joinPoint)

        return if (cacheEvict.beforeInvocation) {
            mono { cacheProvider.value.delete(key) }.flatMap { target }
        } else {
            target.flatMap { result ->
                mono { cacheProvider.value.delete(key) }.thenReturn(result)
            }
        }
    }

}
