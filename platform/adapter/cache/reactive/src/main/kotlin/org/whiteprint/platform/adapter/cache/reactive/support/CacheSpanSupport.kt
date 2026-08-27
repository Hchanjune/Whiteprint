package org.whiteprint.platform.adapter.cache.reactive.support

import io.github.hchanjune.omk.core.context.ManagedContext
import io.github.hchanjune.omk.core.metric.SpanSupport
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import io.github.hchanjune.omk.reactive.ReactiveOperations
import org.aspectj.lang.ProceedingJoinPoint

/**
 * 캐시 애스펙트가 여는 `[CAC]` 스팬(reactive). servlet 쪽 `CacheSpanSupport` 와 같은 의도다.
 *
 * ## 왜 필요한가
 * 애스펙트가 `ManagedRepositoryAspect` 보다 **바깥**이라 캐시 히트면 `[DB ]` 스팬이 안 생긴다.
 * 그 자리를 캐시 자신의 레이어로 채운다 — 히트는 `[CAC]` 하나, 미스는 `[CAC]` 안에 `[DB ]`.
 *
 * ## servlet 과 다른 점
 * 컨텍스트가 ThreadLocal 이 아니라 **Reactor 컨텍스트**에 있고, 구독 시점에야 읽을 수 있다.
 * 그래서 `transformDeferredContextual` 로 미뤄서 스팬을 연다 — 조립 시점에 열면
 * 구독되지 않은 파이프라인에도 스팬이 생기고, 끝나지 않은 채 남는다.
 * OMK 의 `ManagedRepositoryAspect`(reactive)가 쓰는 방식과 같다.
 *
 * ## ⚠ `@ManagedCacheRepository` 와 같이 쓰지 말 것
 * 그 애노테이션도 `[CAC]` 레이어를 여는데 이 애스펙트보다 **안쪽**이다(`HIGHEST + 15`).
 * 둘을 같이 걸면 미스일 때 `[CAC]` 안에 `[CAC]` 가 겹친다 — 거의 같은 구간을 두 번 재는 셈이다.
 *
 * 반대로 "`@ManagedCacheRepository` 가 있으면 여기서 건너뛰기"는 **안 된다**.
 * 히트일 때는 우리가 바깥이라 안쪽 애스펙트가 아예 실행되지 않으므로, 건너뛰면 히트에 스팬이 없어진다.
 *
 * → `@Cached` 계열을 쓰는 클래스에는 `@ManagedCacheRepository` 를 붙이지 않는다.
 * 그 애노테이션은 애노테이션 없이 캐시를 직접 다루는 클래스용으로 남는다.
 */
internal object CacheSpanSupport {

    fun aroundMono(
        joinPoint: ProceedingJoinPoint,
        spanIdProvider: SpanIdProvider,
        block: () -> Any?,
    ): Any? {
        val className = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name.substringBefore('-')

        return asMono(block()).transformDeferredContextual { source, reactorContext ->
            val context = reactorContext
                .getOrEmpty<ManagedContext>(ReactiveOperations.CONTEXT_KEY)
                .orElse(null)
                ?: return@transformDeferredContextual source

            val span = SpanSupport.pushCacheSpan(context, className, methodName, spanIdProvider)

            source
                .doOnSuccess { span.end(); context.pop() }
                .doOnError { exception -> span.end(exception); context.pop() }
        }
    }

}
