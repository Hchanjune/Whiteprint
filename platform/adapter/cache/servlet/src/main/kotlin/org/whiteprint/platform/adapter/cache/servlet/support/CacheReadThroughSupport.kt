package org.whiteprint.platform.adapter.cache.servlet.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.policy.CacheException
import org.whiteprint.platform.core.cache.policy.CachePolicy
import org.whiteprint.platform.core.cache.provider.CacheProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

/** `@Cached`와 `@Idempotent`가 공유하는 "캐시에 있으면 반환, 없으면 실행 후 저장" 로직. */
internal object CacheReadThroughSupport {

    fun <K : Annotation> readThroughOrProceed(
        joinPoint: ProceedingJoinPoint,
        cacheProvider: CacheProvider,
        keyAnnotationClass: Class<K>,
        keyOrderOf: (K) -> Int,
        prefix: String,
        ttl: Long,
        timeUnit: TimeUnit,
    ): Any? {
        val key = buildKey(joinPoint, keyAnnotationClass, keyOrderOf, prefix)

        cacheProvider.value.raw(key)?.let { return it }

        val result = joinPoint.proceed()
        if (result != null) {
            cacheProvider.value.setWithTtl(key, result, Duration.ofMillis(timeUnit.toMillis(ttl)))
        }
        return result
    }

    fun <K : Annotation> buildKey(
        joinPoint: ProceedingJoinPoint,
        keyAnnotationClass: Class<K>,
        keyOrderOf: (K) -> Int,
        prefix: String,
    ): CacheKey {
        val entries = CacheKeyResolver.resolve(joinPoint, keyAnnotationClass, keyOrderOf)
        if (entries.isEmpty()) {
            val method = (joinPoint.signature as MethodSignature).method
            throw CacheException(CachePolicy.NO_CACHE_KEY_DEFINED, mapOf("key" to method.name))
        }
        val keyPart = CacheKeyResolver.buildKeyPart(entries)
        return if (prefix.isBlank()) CacheKey(keyPart) else CacheKey("$prefix:$keyPart")
    }

}
