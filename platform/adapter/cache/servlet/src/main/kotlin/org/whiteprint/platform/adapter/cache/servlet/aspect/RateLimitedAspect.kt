package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.whiteprint.platform.adapter.cache.common.aspect.CacheAspectOrder
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.servlet.support.CacheSpanSupport
import org.whiteprint.platform.core.cache.annotation.RateLimited
import org.whiteprint.platform.core.cache.annotation.RateLimitedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.CacheProvider
import java.time.Duration

@Aspect
@Order(CacheAspectOrder.RATE_LIMITED)
class RateLimitedAspect(
    private val cacheProvider: CacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(rateLimited)")
    fun around(joinPoint: ProceedingJoinPoint, rateLimited: RateLimited): Any? =
        CacheSpanSupport.around(joinPoint, spanIdProvider) { proceed(joinPoint, rateLimited) }

    private fun proceed(joinPoint: ProceedingJoinPoint, rateLimited: RateLimited): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = RateLimitedKey::class.java,
            keyNameOf = { it.name },
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
