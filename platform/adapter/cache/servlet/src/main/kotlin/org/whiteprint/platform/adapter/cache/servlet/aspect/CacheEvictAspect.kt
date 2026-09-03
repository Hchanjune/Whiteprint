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
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.provider.CacheProvider

@Aspect
@Order(CacheAspectOrder.CACHE_EVICT)
class CacheEvictAspect(
    private val cacheProvider: CacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    /**
     * ⚠ **대상 메서드를 `[CAC]` 스팬으로 감싸지 않는다.** `@Cached` 와 달리 무효화는 본문을 대체하지 않는다 —
     * 통째로 감싸면 본문 시간(대개 DB)이 전부 캐시로 귀속돼 트레이스가 거꾸로 읽힌다.
     * 스팬은 **삭제 호출 하나**에만 연다.
     */
    @Around("@annotation(cacheEvict)")
    fun around(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = CacheEvictKey::class.java,
            keyNameOf = { it.name },
            prefix = cacheEvict.prefix,
        )

        if (cacheEvict.beforeInvocation) {
            evict(joinPoint, key)
            return joinPoint.proceed()
        }

        val result = joinPoint.proceed()
        evict(joinPoint, key)
        return result
    }

    private fun evict(joinPoint: ProceedingJoinPoint, key: CacheKey) =
        CacheSpanSupport.aroundCacheCall(joinPoint, spanIdProvider, EVICT_SPAN) {
            cacheProvider.value.delete(key)
        }

    companion object {
        private const val EVICT_SPAN = "evict"
    }

}
