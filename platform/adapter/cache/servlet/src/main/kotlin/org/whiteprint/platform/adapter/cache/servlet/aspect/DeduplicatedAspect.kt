package org.whiteprint.platform.adapter.cache.servlet.aspect

import org.whiteprint.platform.adapter.cache.common.aspect.CacheAspectOrder
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.whiteprint.platform.adapter.cache.servlet.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.servlet.support.CacheSpanSupport
import org.whiteprint.platform.core.cache.annotation.Deduplicated
import org.whiteprint.platform.core.cache.annotation.DeduplicatedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.CacheProvider
import java.time.Duration

@Aspect
@Order(CacheAspectOrder.DEDUPLICATED)
class DeduplicatedAspect(
    private val cacheProvider: CacheProvider,
    private val spanIdProvider: SpanIdProvider,
) {

    @Around("@annotation(deduplicated)")
    fun around(joinPoint: ProceedingJoinPoint, deduplicated: Deduplicated): Any? =
        CacheSpanSupport.around(joinPoint, spanIdProvider) { proceed(joinPoint, deduplicated) }

    private fun proceed(joinPoint: ProceedingJoinPoint, deduplicated: Deduplicated): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = DeduplicatedKey::class.java,
            keyNameOf = { it.name },
            prefix = deduplicated.prefix,
        )
        val ttl = Duration.ofMillis(deduplicated.timeUnit.toMillis(deduplicated.ttl))

        val firstSeen = cacheProvider.value.setIfAbsentWithTtl(key, true, ttl)
        if (!firstSeen) {
            throw CacheException(CachePolicy.DUPLICATE_REQUEST, mapOf("key" to key.value))
        }

        return joinPoint.proceed()
    }

}
