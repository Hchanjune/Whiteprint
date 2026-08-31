package org.whiteprint.platform.adapter.cache.reactive.support

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.context.ManagedContext
import io.github.hchanjune.omk.reactive.ReactiveOperations
import org.whiteprint.platform.adapter.cache.common.support.CacheKeyResolver
import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.model.CachedOperation
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

        // proceed() 는 switchIfEmpty 안이 아니라 여기서 불러야 한다 — 이유는 proceedCold 참조.
        // 콜드 Mono 라 캐시 히트면 구독하지 않고 그대로 버린다.
        val target = proceedCold(joinPoint)

        // 히트 여부를 남길 [CAC] 스팬은 CacheSpanSupport 가 바깥에서 이미 열어뒀다.
        // servlet 과 달리 ThreadLocal 이 아니라 Reactor 컨텍스트에 있으므로 구독 시점에 꺼내야 한다.
        return Mono.deferContextual { reactorContext ->
            val managedContext = reactorContext
                .getOrEmpty<ManagedContext>(ReactiveOperations.CONTEXT_KEY)
                .orElse(null)
            val span = managedContext?.peek()

            // mono { }는 block이 null을 반환하면 empty Mono가 되므로, "캐시 없음"이 자연스럽게 switchIfEmpty로 넘어간다.
            mono { cacheProvider.value.raw(key)?.takeIf { canRestore(it, managedContext) } }
                .doOnNext { span?.markCacheHit(true) }
                .map { cached -> restore(cached, managedContext) }
                .switchIfEmpty(
                    Mono.defer {
                        span?.markCacheHit(false)
                        target.flatMap { value ->
                            mono { cacheProvider.value.setWithTtl(key, store(value), ttlDuration) }.thenReturn(value)
                        }
                    }
                )
        }
    }

    /**
     * 유스케이스에 `@Cached` 를 붙일 수 있게 하는 부분. servlet 쪽과 같은 판단이다.
     *
     * 판단 기준은 **반환값이 `OperationResult` 인지**다 — `@ManagedOperation` 유무가 아니다.
     * 그 애노테이션 없이도 `OperationResult` 를 돌려주는 메서드가 있을 수 있다.
     *
     * `data` 만 떼어 [CachedOperation] 봉투에 담는다 — 같이 들어 있는 `ManagedContext` 에
     * traceId·ip·deviceId 가 있어서 통째로 캐싱하면 남의 요청 정보가 박제된다.
     *
     * ⚠ suspend 함수는 JVM 반환 타입이 `Object` 라 시그니처로는 판별할 수 없다.
     * 그래서 타입이 아니라 **값**을 보고 판단한다.
     */
    private fun store(result: Any): Any =
        if (result is OperationResult<*>) CachedOperation(result.data as Any) else result

    /**
     * 봉투를 다시 `OperationResult` 로 조립할 수 있는 상황인지. **컨텍스트가 있어야 한다.**
     *
     * 없으면 캐시된 값을 흘려보내고(빈 Mono) 원래 메서드를 실행한다 — 맨 데이터를 돌려주면 선언
     * 반환 타입이 `OperationResult` 인 호출부에서 `ClassCastException` 이 난다.
     */
    private fun canRestore(cached: Any, managedContext: ManagedContext?): Boolean =
        cached !is CachedOperation || managedContext != null

    /** 봉투에 담긴 값이면 **지금 파이프라인의 컨텍스트**로 다시 조립한다. */
    private fun restore(cached: Any, managedContext: ManagedContext?): Any =
        if (cached is CachedOperation && managedContext != null) {
            OperationResult(managedContext, cached.data)
        } else {
            cached
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
