package org.whiteprint.platform.adapter.cache.servlet.support

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.servlet.Operations
import org.whiteprint.platform.core.cache.model.CachedOperation
import org.whiteprint.platform.adapter.cache.common.support.CacheKeyResolver
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
        keyNameOf: (K) -> String,
        prefix: String,
        ttl: Long,
        timeUnit: TimeUnit,
    ): Any? {
        val key = buildKey(joinPoint, keyAnnotationClass, keyNameOf, prefix)

        val cached = cacheProvider.value.raw(key)
        if (cached != null && canRestore(cached)) {
            markCacheHit(true)
            return restore(cached)
        }
        markCacheHit(false)

        val result = joinPoint.proceed()
        if (result != null) {
            cacheProvider.value.setWithTtl(key, store(result), Duration.ofMillis(timeUnit.toMillis(ttl)))
        }
        return result
    }

    /**
     * 유스케이스에 `@Cached` 를 붙일 수 있게 하는 부분.
     *
     * 판단 기준은 **반환값이 `OperationResult` 인지**다 — `@ManagedOperation` 유무가 아니다.
     * 그 애노테이션 없이도 `OperationResult` 를 돌려주는 메서드가 있을 수 있고, 그쪽도 똑같이 다뤄야 한다.
     *
     * `data` 만 떼어 [CachedOperation] 봉투에 담는다 — 같이 들어 있는 `ManagedContext` 에
     * traceId·ip·deviceId 가 있어서 통째로 캐싱하면 남의 요청 정보가 박제된다.
     */
    private fun store(result: Any): Any =
        if (result is OperationResult<*>) CachedOperation(result.data as Any) else result

    /**
     * 봉투를 다시 `OperationResult` 로 조립할 수 있는 상황인지. **컨텍스트가 있어야 한다.**
     *
     * 없으면 캐시를 쓰지 않고 원래 메서드를 실행한다 — 맨 데이터를 돌려주면 선언 반환 타입이
     * `OperationResult` 인 호출부에서 `ClassCastException` 이 난다. 없는 컨텍스트를 지어내는 것도
     * 답이 아니다(가짜 traceId 가 트레이스에 섞인다). 감쌀 수 없으면 그 캐시는 이번 호출에 못 쓰는 것뿐이다.
     */
    private fun canRestore(cached: Any): Boolean =
        cached !is CachedOperation || Operations.hasContext

    /**
     * 봉투에 담긴 값이면 **지금 들어온 요청의 컨텍스트**로 `OperationResult` 를 다시 조립한다.
     * 캐시된 것은 순수 데이터뿐이고 관측 정보는 언제나 현재 요청의 것이 된다.
     */
    private fun restore(cached: Any): Any =
        if (cached is CachedOperation) OperationResult(Operations.context, cached.data) else cached

    /**
     * 열려 있는 `[CAC]` 스팬에 히트 여부를 남긴다. 그 스팬은 [CacheSpanSupport] 가 **바깥에서** 이미
     * 열어뒀으므로 `peek()` 이 그것을 가리킨다 — 애스펙트 시그니처로 스팬을 넘길 필요가 없다.
     *
     * 컨텍스트가 없으면(진입점 밖) 스팬도 없으므로 조용히 넘어간다.
     */
    private fun markCacheHit(hit: Boolean) {
        if (!Operations.hasContext) return
        Operations.context.peek()?.markCacheHit(hit)
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
