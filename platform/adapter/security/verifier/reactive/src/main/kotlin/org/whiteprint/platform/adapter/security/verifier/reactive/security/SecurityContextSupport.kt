package org.whiteprint.platform.adapter.security.verifier.reactive.security

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import reactor.core.publisher.Mono

/**
 * 현재 요청의 인증 주체를 꺼낸다. 서블릿 쪽 `SecurityContextSupport` 와 대칭이며,
 * `...OrNull`/`...OrEmpty` 가 따로 있는 이유(옵셔널 인증)도 같다 —
 * `permitted-entry-points` 경로는 토큰이 없어도 통과하므로 인증이 없는 것이 정상이다.
 *
 * **없음은 빈 Mono 로 표현하고 예외로 만들지 않는다.** 이게 [currentClaimsOrEmpty] 와
 * [currentClaims] 를 가르는 유일한 차이이고, 던지는 쪽은 빈 Mono 를 예외로 바꾸는 얇은 겹이다.
 */
object SecurityContextSupport {

    /**
     * 인증이 없거나 토큰 기반이 아니면 **빈 Mono**. 예외를 던지지 않는다.
     *
     * [VerifiedUser] 를 먼저 보고 principal 을 나중에 보는 것은 이 필터 체인이 세우는 것이 전자이기
     * 때문이다. 후자는 다른 방식으로 채워진 컨텍스트(테스트·커스텀 필터)를 받아주기 위한 통로다.
     *
     * 익명 토큰은 principal 이 `"anonymousUser"` 문자열이라 두 분기 어디에도 걸리지 않는다 —
     * 즉 여기는 이미 익명에 안전하다. [currentSubjectOrEmpty] 는 따로 걸러낸다.
     */
    fun currentClaimsOrEmpty(): Mono<AccessTokenClaims> =
        ReactiveSecurityContextHolder.getContext()
            .mapNotNull<AccessTokenClaims> { ctx ->
                when (val auth = ctx.authentication) {
                    is VerifiedUser -> auth.claims
                    else -> auth?.principal as? AccessTokenClaims
                }
            }

    /** 인증이 필수인 경로용. 없으면 [SecurityPolicy.TOKEN_NOT_FOUND]. */
    fun currentClaims(): Mono<AccessTokenClaims> =
        currentClaimsOrEmpty()
            .switchIfEmpty(Mono.error { SecurityException(SecurityPolicy.TOKEN_NOT_FOUND) })

    /**
     * 인증이 없으면 **빈 Mono**.
     *
     * ⚠ **빈 subject 도 없는 것으로 본다.** `claims.subject` 에 하한이 없어서 subject 없이 발급된
     * 토큰이 빈 문자열로 통과할 수 있고, 그대로 흘려보내면 호출부의 `toLong()` 이 500 이 되거나
     * 빈 값이 조회 조건에 들어가 필터가 무력화된다.
     *
     * ⚠ 익명 토큰은 subject 가 아니라 principal 로 걸러진다([currentClaimsOrEmpty] 참고).
     * WebFlux 는 서블릿과 달리 익명 인증이 기본 비활성이지만, 켜더라도 동작이 달라지지 않는다.
     */
    fun currentSubjectOrEmpty(): Mono<String> =
        currentClaimsOrEmpty().mapNotNull { it.subject.takeUnless(String::isBlank) }

    /** 인증이 필수인 경로용. 없으면 [SecurityPolicy.TOKEN_NOT_FOUND]. */
    fun currentSubject(): Mono<String> =
        currentSubjectOrEmpty()
            .switchIfEmpty(Mono.error { SecurityException(SecurityPolicy.TOKEN_NOT_FOUND) })

    suspend fun awaitCurrentClaims(): AccessTokenClaims = currentClaims().awaitSingle()

    suspend fun awaitCurrentClaimsOrNull(): AccessTokenClaims? = currentClaimsOrEmpty().awaitSingleOrNull()

    suspend fun awaitCurrentSubject(): String = currentSubject().awaitSingle()

    suspend fun awaitCurrentSubjectOrNull(): String? = currentSubjectOrEmpty().awaitSingleOrNull()
}
