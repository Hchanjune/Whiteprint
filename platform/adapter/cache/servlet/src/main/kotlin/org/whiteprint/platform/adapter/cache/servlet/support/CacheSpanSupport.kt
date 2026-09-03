package org.whiteprint.platform.adapter.cache.servlet.support

import io.github.hchanjune.omk.core.metric.SpanSupport
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import io.github.hchanjune.omk.servlet.Operations
import org.aspectj.lang.ProceedingJoinPoint

/**
 * 캐시 애스펙트가 여는 `[CAC]` 스팬.
 *
 * ## 왜 필요한가
 * 애스펙트가 `ManagedRepositoryAspect` 보다 **바깥**에 있으므로([CacheAspectOrder]),
 * 캐시 히트면 `[DB ]` 스팬이 아예 생기지 않는다. 그 자리에 아무 스팬도 없으면
 * 트레이스에 설명 없는 공백이 남는다 — Redis 왕복은 분명히 일어났는데도.
 *
 * 그래서 캐시 자신의 레이어를 연다. 결과적으로 트레이스가 정확해진다:
 * - 히트: `[CAC]` 하나
 * - 미스: `[CAC]` 안에 `[DB ]` 가 중첩 (캐시가 바깥이므로 자연히 그렇게 된다)
 *
 * ## ⚠ [around] 는 `@Cached` 계열 전용이다
 * 대상 메서드를 통째로 감싸므로 "캐시가 그 구간을 대체했다"가 참일 때만 정확하다.
 * `@CacheEvict` 는 본문을 대체하지 않고 삭제만 곁들이므로 [aroundCacheCall] 로 **삭제 호출만** 감싼다 —
 * 통째로 감싸면 본문 시간이 전부 캐시로 귀속돼 트레이스가 거꾸로 읽힌다.
 *
 * ## 컨텍스트가 없으면 그냥 통과시킨다
 * 진입점 밖(테스트, 초기화 등)에서 불릴 수 있고, 그때 스팬을 억지로 만들면
 * 부모 없는 고아 스팬이 생긴다.
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

    fun <T> around(
        joinPoint: ProceedingJoinPoint,
        spanIdProvider: SpanIdProvider,
        block: () -> T,
    ): T {
        if (!Operations.hasContext) return block()

        val context = Operations.context
        val className = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name.substringBefore('-')

        val span = SpanSupport.pushCacheSpan(context, className, methodName, spanIdProvider)

        return try {
            val result = block()
            span.end()
            context.pop()
            result
        } catch (exception: Throwable) {
            span.end(exception)
            context.pop()
            throw exception
        }
    }

    /**
     * 캐시 **호출 하나**만 감싸는 leaf 스팬. `@CacheEvict` 처럼 대상 메서드를 대체하지 않는 경우에 쓴다.
     * [operation] 은 캐시 동작 이름(`evict` 등)이다 — 메서드명을 쓰면 본문을 잰 것처럼 보인다.
     */
    fun <T> aroundCacheCall(
        joinPoint: ProceedingJoinPoint,
        spanIdProvider: SpanIdProvider,
        operation: String,
        block: () -> T,
    ): T {
        if (!Operations.hasContext) return block()

        val context = Operations.context
        val className = joinPoint.signature.declaringType.simpleName

        val span = SpanSupport.pushCacheSpan(context, className, operation, spanIdProvider)

        return try {
            val result = block()
            span.end()
            context.pop()
            result
        } catch (exception: Throwable) {
            span.end(exception)
            context.pop()
            throw exception
        }
    }

}
