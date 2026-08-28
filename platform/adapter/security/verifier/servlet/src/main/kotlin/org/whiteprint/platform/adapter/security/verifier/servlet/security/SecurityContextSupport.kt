package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy

/**
 * 현재 요청의 인증 주체를 꺼낸다.
 *
 * `...OrNull` 쌍이 따로 있는 것은 **옵셔널 인증** 때문이다 — `permitted-entry-points` 에 등록된 경로는
 * 토큰이 없거나 유효하지 않아도 통과한다([StatelessSecurityFilter]). 그런 경로에서 인증은
 * "있으면 개인화, 없으면 익명"이라 없는 것이 정상이고, 예외로 다룰 일이 아니다.
 *
 * 그 경로에서 [getCurrentSubject] 를 쓰면 비로그인 요청이 401 로 막혀 permitted 로 연 의미가 사라진다.
 * 반대로 인증이 필수인 경로에서 [getCurrentSubjectOrNull] 을 쓰면 null 이 아래로 흘러가므로,
 * **경로의 성격에 맞는 쪽을 골라야 한다.**
 *
 * 리액티브 쪽 `SecurityContextSupport` 의 `awaitCurrentClaimsOrNull`/`awaitCurrentSubjectOrNull` 과 대칭이다.
 */
object SecurityContextSupport {

    /**
     * 인증이 없거나 토큰 기반이 아니면 null. 예외를 던지지 않는다.
     *
     * [VerifiedUser] 를 먼저 보고 principal 을 나중에 보는 것은 이 필터 체인이 세우는 것이 전자이기
     * 때문이다. 후자는 다른 방식으로 채워진 컨텍스트(테스트·커스텀 필터)를 받아주기 위한 통로다.
     */
    fun getCurrentClaimsOrNull(): AccessTokenClaims? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null

        // 익명 토큰은 principal 이 "anonymousUser" 문자열이라 아래 두 분기에 걸리지 않는다 —
        // 즉 여기는 이미 익명에 안전하다. subject 쪽은 name 을 보기 때문에 따로 걸러내야 한다.
        if (auth is VerifiedUser) {
            return auth.claims
        }

        return auth.principal as? AccessTokenClaims
    }

    /** 인증이 필수인 경로용. 없으면 [SecurityPolicy.TOKEN_NOT_FOUND]. */
    fun getCurrentClaims(): AccessTokenClaims =
        getCurrentClaimsOrNull()
            ?: throw SecurityException(policy = SecurityPolicy.TOKEN_NOT_FOUND)

    /**
     * 인증이 없으면 null.
     *
     * ⚠ **[AnonymousAuthenticationToken] 을 명시적으로 걸러낸다.** 서블릿 체인은 익명 인증이 기본
     * 활성이라(WebFlux 는 반대로 기본 비활성이다) 비로그인 요청에서도 `authentication` 이 null 이 아니라
     * 이 토큰이고, `name` 은 `"anonymousUser"` 라는 **비어 있지 않은 문자열**이다.
     * 걸러내지 않으면 호출부의 `?.toLong()` 이 그 문자열을 만나 500 이 된다.
     * 여기서 막아두면 체인에서 `.anonymous()` 를 끄든 켜든 동작이 같다.
     *
     * ⚠ **빈 subject 도 null 로 본다.** `VerifiedUser.getName()` 은 `claims.subject` 를 그대로
     * 돌려주는데 그 값에 하한이 없어서, subject 없이 발급된 토큰이 빈 문자열로 통과할 수 있다.
     * 그대로 흘려보내면 호출부에서 `"".toLong()`(500)이 되거나, 더 나쁘게는
     * 빈 값이 조회 조건에 들어가 필터가 무력화된다.
     */
    fun getCurrentSubjectOrNull(): String? =
        SecurityContextHolder.getContext().authentication
            ?.takeUnless { it is AnonymousAuthenticationToken }
            ?.name
            ?.takeUnless { it.isBlank() }

    /** 인증이 필수인 경로용. 없으면 [SecurityPolicy.TOKEN_NOT_FOUND]. */
    fun getCurrentSubject(): String =
        getCurrentSubjectOrNull()
            ?: throw SecurityException(policy = SecurityPolicy.TOKEN_NOT_FOUND)
}
