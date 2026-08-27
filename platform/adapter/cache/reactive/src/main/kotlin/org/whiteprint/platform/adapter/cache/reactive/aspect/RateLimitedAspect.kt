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
import org.whiteprint.platform.adapter.cache.reactive.support.asMono
import org.whiteprint.platform.core.cache.annotation.RateLimited
import org.whiteprint.platform.core.cache.annotation.RateLimitedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import java.time.Duration

@Aspect
@Order(CacheAspectOrder.RATE_LIMITED)
class RateLimitedAspect(
    private val cacheProvider: ReactiveCacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(rateLimited)")
    fun around(joinPoint: ProceedingJoinPoint, rateLimited: RateLimited): Any? =
        CacheSpanSupport.aroundMono(joinPoint, spanIdProvider) { proceed(joinPoint, rateLimited) }

    private fun proceed(joinPoint: ProceedingJoinPoint, rateLimited: RateLimited): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = RateLimitedKey::class.java,
            keyNameOf = { it.name },
            prefix = rateLimited.prefix,
        )
        val ttl = Duration.ofMillis(rateLimited.timeUnit.toMillis(rateLimited.ttl))

        return mono {
            cacheProvider.atomic.incrementWithLimitAndExpireOrThrow(
                key = key,
                delta = 1L,
                limit = rateLimited.limit,
                ttl = ttl,
            )
        }.onErrorMap(CacheException::class.java) { e ->
            if (e.policy == CachePolicy.INCREMENT_LIMIT_EXCEEDED) {
                CacheException(
                    CachePolicy.RATE_LIMIT_EXCEEDED,
                    mapOf("key" to key.value, "limit" to rateLimited.limit),
                    cause = e,
                )
            } else {
                e
            }
        }.flatMap { asMono(joinPoint.proceed()) }
    }

}
