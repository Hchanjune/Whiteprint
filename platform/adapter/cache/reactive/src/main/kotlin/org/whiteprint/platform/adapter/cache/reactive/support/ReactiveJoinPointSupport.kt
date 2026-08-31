package org.whiteprint.platform.adapter.cache.reactive.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import reactor.core.publisher.Mono
import kotlin.coroutines.Continuation

/**
 * Spring AOP 프록시는 suspend 메서드 호출을 이미 Mono로 브릿징해서 넘겨준다
 * (joinPoint.proceed()가 Mono<*>를 반환). 혹시 대상이 진짜 동기 메서드거나 null이어도
 * 안전하게 Mono로 통일해서 다루기 위한 헬퍼.
 */
@Suppress("UNCHECKED_CAST")
internal fun asMono(result: Any?): Mono<Any> = when (result) {
    is Mono<*> -> result as Mono<Any>
    null -> Mono.empty()
    else -> Mono.just(result)
}

/**
 * 대상 호출을 **지금**(어드바이스에 진입한 스레드에서) 조립해 콜드 Mono 로 돌려준다.
 * 실행 여부는 **구독하느냐 마느냐로만** 정한다 — 캐시 히트나 중복 요청이면 구독하지 않으면 그만이다.
 *
 * ## ⚠ `proceed()` 를 연산자 안에서 부르면 안 된다
 * `flatMap`/`defer` 안에서 부르면 그 시점엔 어드바이스가 이미 반환했고 스레드도 바뀌어 있다.
 * Spring 이 `@annotation(...)`/`@within(...)` 의 인자를 넘길 때 쓰는 바인딩은
 * `ExposeInvocationInterceptor` 의 ThreadLocal 에 걸려 있어서, 거기서 체인을 이어가면
 * **안쪽 어드바이스**가 자기 인자를 못 받고 터진다:
 * ```
 * IllegalStateException: Required to bind 2 arguments, but only bound 1
 *                        (JoinPointMatch was NOT bound in invocation)
 * ```
 * 대상이 suspend/`Mono` 면 `proceed()` 는 파이프라인을 **조립만** 하므로 미리 불러도 안전하다 —
 * 본문은 구독해야 돈다. OMK 의 리액티브 애스펙트들이 전부 이 방식이고, Spring 자신의
 * 리액티브 `@Cacheable` 도 같다.
 *
 * ## ⚠ 미리 부르면 생기는 비용
 * 구독하지 않고 버리는 경우에도 **안쪽 어드바이스의 "조립 시점 부작용"은 이미 일어난다.**
 * 안쪽이 스팬을 미리 여는 종류(OMK `@ManagedRepository`·`@ManagedExternalCall`·`@ManagedMetric` 의
 * suspend 경로)면 **그 스팬이 닫히지 않고 남는다.**
 * → 캐시 애노테이션을 그것들과 **같은 메서드에 겹쳐 쓰지 않는다.**
 * (`@ManagedOperation` 은 캐시보다 바깥이라 무관하고, `@ManagedService` 는 스팬을 열지 않아 안전하다.)
 *
 * 대상이 동기 메서드면 미리 부르는 순간 **본문이 실행돼** 캐시·중복 판정 자체가 무의미해진다.
 * 그때만 지연시키며, 그 경로에는 위 바인딩 문제가 그대로 남는다 —
 * 리액티브 모듈에 동기 대상을 넣지 않는 것이 전제다.
 */
internal fun proceedCold(joinPoint: ProceedingJoinPoint): Mono<Any> =
    if (isColdOnProceed(joinPoint)) asMono(joinPoint.proceed())
    else Mono.defer { asMono(joinPoint.proceed()) }

/** suspend 이거나 `Mono` 반환이면 `proceed()` 가 조립만 하고 본문은 구독 시점에 돈다. */
private fun isColdOnProceed(joinPoint: ProceedingJoinPoint): Boolean {
    val method = (joinPoint.signature as MethodSignature).method
    if (Mono::class.java.isAssignableFrom(method.returnType)) return true
    return method.parameterTypes.lastOrNull()
        ?.let { Continuation::class.java.isAssignableFrom(it) } == true
}
