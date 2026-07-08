package org.whiteprint.platform.adapter.cache.reactive.aspect

import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.whiteprint.platform.adapter.cache.reactive.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.reactive.support.asMono
import org.whiteprint.platform.core.cache.annotation.RateLimited
import org.whiteprint.platform.core.cache.annotation.RateLimitedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import java.time.Duration

@Aspect
class RateLimitedAspect(
    private val cacheProvider: ReactiveCacheProvider,
) {

    @Around("@annotation(rateLimited)")
    fun around(joinPoint: ProceedingJoinPoint, rateLimited: RateLimited): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = RateLimitedKey::class.java,
            keyOrderOf = { it.order },
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
