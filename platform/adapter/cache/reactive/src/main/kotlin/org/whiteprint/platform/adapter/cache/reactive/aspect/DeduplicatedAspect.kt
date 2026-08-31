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
import org.whiteprint.platform.core.cache.annotation.Deduplicated
import org.whiteprint.platform.core.cache.annotation.DeduplicatedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import reactor.core.publisher.Mono
import java.time.Duration

@Aspect
@Order(CacheAspectOrder.DEDUPLICATED)
class DeduplicatedAspect(
    private val cacheProvider: ReactiveCacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(deduplicated)")
    fun around(joinPoint: ProceedingJoinPoint, deduplicated: Deduplicated): Any? =
        CacheSpanSupport.aroundMono(joinPoint, spanIdProvider) { proceed(joinPoint, deduplicated) }

    private fun proceed(joinPoint: ProceedingJoinPoint, deduplicated: Deduplicated): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = DeduplicatedKey::class.java,
            keyNameOf = { it.name },
            prefix = deduplicated.prefix,
        )
        val ttl = Duration.ofMillis(deduplicated.timeUnit.toMillis(deduplicated.ttl))

        // proceed() 는 flatMap 안이 아니라 여기서 불러야 한다 — 이유는 proceedCold 참조.
        val target = proceedCold(joinPoint)

        return mono { cacheProvider.value.setIfAbsentWithTtl(key, true, ttl) }
            .flatMap { firstSeen ->
                if (firstSeen) {
                    target
                } else {
                    Mono.error(CacheException(CachePolicy.DUPLICATE_REQUEST, mapOf("key" to key.value)))
                }
            }
    }

}
