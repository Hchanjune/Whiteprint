package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.core.cache.annotation.RateLimited
import org.whiteprint.platform.core.cache.annotation.RateLimitedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.CacheProvider
import java.time.Duration

@Aspect
class RateLimitedAspect(
    private val cacheProvider: CacheProvider,
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

        try {
            cacheProvider.atomic.incrementWithLimitAndExpireOrThrow(
                key = key,
                delta = 1L,
                limit = rateLimited.limit,
                ttl = ttl,
            )
        } catch (e: CacheException) {
            if (e.policy == CachePolicy.INCREMENT_LIMIT_EXCEEDED) {
                throw CacheException(
                    CachePolicy.RATE_LIMIT_EXCEEDED,
                    mapOf("key" to key.value, "limit" to rateLimited.limit),
                    cause = e,
                )
            }
            throw e
        }

        return joinPoint.proceed()
    }

}
