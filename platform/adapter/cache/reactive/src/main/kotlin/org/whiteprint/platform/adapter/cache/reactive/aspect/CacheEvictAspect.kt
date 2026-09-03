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
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import reactor.core.publisher.Mono

@Aspect
@Order(CacheAspectOrder.CACHE_EVICT)
class CacheEvictAspect(
    private val cacheProvider: ReactiveCacheProvider,
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

        // proceed() 는 연산자 안이 아니라 여기서 불러야 한다 — 이유는 proceedCold 참조.
        val target = proceedCold(joinPoint)

        return if (cacheEvict.beforeInvocation) {
            evict(joinPoint, key).flatMap { target }
        } else {
            target.flatMap { result -> evict(joinPoint, key).thenReturn(result) }
        }
    }

    /** 삭제 한 번 = 스팬 하나. `delete` 가 Unit 을 돌려주므로 신호를 하나 실어 스팬이 닫히게 한다. */
    private fun evict(joinPoint: ProceedingJoinPoint, key: CacheKey): Mono<Boolean> =
        CacheSpanSupport.aroundCacheCallMono(
            joinPoint = joinPoint,
            spanIdProvider = spanIdProvider,
            operation = EVICT_SPAN,
            source = mono { cacheProvider.value.delete(key); true },
        )

    companion object {
        private const val EVICT_SPAN = "evict"
    }

}
