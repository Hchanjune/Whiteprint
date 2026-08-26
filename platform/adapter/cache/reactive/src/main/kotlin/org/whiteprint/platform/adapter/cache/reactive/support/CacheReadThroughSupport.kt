package org.whiteprint.platform.adapter.cache.reactive.support

import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeUnit

/** `@Cached`와 `@Idempotent`가 공유하는 "캐시에 있으면 반환, 없으면 실행 후 저장" 로직. */
internal object CacheReadThroughSupport {

    fun <K : Annotation> readThroughOrProceed(
        joinPoint: ProceedingJoinPoint,
        cacheProvider: ReactiveCacheProvider,
        keyAnnotationClass: Class<K>,
        keyNameOf: (K) -> String,
        prefix: String,
        ttl: Long,
        timeUnit: TimeUnit,
    ): Mono<Any> {
        val key = buildKey(joinPoint, keyAnnotationClass, keyNameOf, prefix)
        val ttlDuration = Duration.ofMillis(timeUnit.toMillis(ttl))

        // mono { }는 block이 null을 반환하면 empty Mono가 되므로, "캐시 없음"이 자연스럽게 switchIfEmpty로 넘어간다.
        return mono { cacheProvider.value.raw(key) }
            .switchIfEmpty(
                Mono.defer {
                    asMono(joinPoint.proceed()).flatMap { value ->
                        mono { cacheProvider.value.setWithTtl(key, value, ttlDuration) }.thenReturn(value)
                    }
                }
            )
    }

    fun <K : Annotation> buildKey(
        joinPoint: ProceedingJoinPoint,
        keyAnnotationClass: Class<K>,
        keyNameOf: (K) -> String,
        prefix: String,
    ): CacheKey {
        val entries = CacheKeyResolver.resolve(joinPoint, keyAnnotationClass, keyNameOf)
        if (entries.isEmpty()) {
            val method = (joinPoint.signature as MethodSignature).method
            throw CacheException(CachePolicy.NO_CACHE_KEY_DEFINED, mapOf("key" to method.name))
        }
        val keyPart = CacheKeyResolver.buildKeyPart(entries)
        return if (prefix.isBlank()) CacheKey(keyPart) else CacheKey("$prefix:$keyPart")
    }

}
