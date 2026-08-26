package org.whiteprint.platform.adapter.cache.reactive.aspect

import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.whiteprint.platform.adapter.cache.reactive.support.CacheReadThroughSupport
import org.whiteprint.platform.adapter.cache.reactive.support.asMono
import org.whiteprint.platform.core.cache.annotation.Deduplicated
import org.whiteprint.platform.core.cache.annotation.DeduplicatedKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import reactor.core.publisher.Mono
import java.time.Duration

@Aspect
class DeduplicatedAspect(
    private val cacheProvider: ReactiveCacheProvider,
) {

    @Around("@annotation(deduplicated)")
    fun around(joinPoint: ProceedingJoinPoint, deduplicated: Deduplicated): Any? {
        val key = CacheReadThroughSupport.buildKey(
            joinPoint = joinPoint,
            keyAnnotationClass = DeduplicatedKey::class.java,
            keyNameOf = { it.name },
            prefix = deduplicated.prefix,
        )
        val ttl = Duration.ofMillis(deduplicated.timeUnit.toMillis(deduplicated.ttl))

        return mono { cacheProvider.value.setIfAbsentWithTtl(key, true, ttl) }
            .flatMap { firstSeen ->
                if (firstSeen) {
                    asMono(joinPoint.proceed())
                } else {
                    Mono.error(CacheException(CachePolicy.DUPLICATE_REQUEST, mapOf("key" to key.value)))
                }
            }
    }

}
