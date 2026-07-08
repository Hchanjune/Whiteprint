package org.whiteprint.platform.adapter.security.verifier.reactive.filter

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.whiteprint.platform.adapter.security.verifier.reactive.security.PermittedPathProvider
import org.whiteprint.platform.adapter.security.verifier.reactive.security.VerifiedUser
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import org.whiteprint.platform.core.security.verifier.AccessTokenVerifier
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class StatelessWebSecurityFilter(
    private val permittedPathProvider: PermittedPathProvider,
    private val accessTokenVerifier: AccessTokenVerifier,
) : WebFilter {

    companion object {
        const val SECURITY_EXCEPTION_KEY = "SECURITY_EXCEPTION"
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return Mono.fromCallable { verifyToken(exchange) }
            .subscribeOn(Schedulers.boundedElastic())
            // verifyToken()만 blocking-tolerant 스레드에서 돌리고, 그 뒤(컨트롤러/서비스/DB)는
            // 다시 가벼운 스케줄러로 돌아온다 — 안 그러면 subscribeOn이 체인 전체를 물들여버린다.
            .publishOn(Schedulers.parallel())
            .flatMap { result ->
                when (result) {
                    is VerificationResult.Authenticated -> {
                        val auth = VerifiedUser(
                            claims = result.claims,
                            authorities = result.claims.permissions.map { SimpleGrantedAuthority(it) }
                        )
                        chain.filter(exchange)
                            .contextWrite(
                                ReactiveSecurityContextHolder.withSecurityContext(
                                    Mono.just(SecurityContextImpl(auth))
                                )
                            )
                    }
                    is VerificationResult.Failed -> {
                        // permitted 경로는 토큰이 없거나 유효하지 않아도 막지 않는다(옵셔널 인증) —
                        // 그래서 이 경우엔 예외를 기록하지 않고 그냥 통과시킨다.
                        if (!permittedPathProvider.isPermitted(exchange.request)) {
                            exchange.attributes[SECURITY_EXCEPTION_KEY] = result.exception
                        }
                        chain.filter(exchange)
                    }
                }
            }
    }

    private fun verifyToken(exchange: ServerWebExchange): VerificationResult {
        return try {
            val token = extractToken(exchange)
                ?: return VerificationResult.Failed(SecurityException(SecurityPolicy.TOKEN_NOT_FOUND))
            val claims = accessTokenVerifier.verifyOrThrow(AccessToken(token))
            VerificationResult.Authenticated(claims)
        } catch (e: SecurityException) {
            VerificationResult.Failed(e)
        } catch (e: Exception) {
            VerificationResult.Failed(SecurityException(SecurityPolicy.TOKEN_VERIFICATION_INTERNAL_ERROR, cause = e))
        }
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val headerValue = exchange.request.headers.getFirst(accessTokenVerifier.headerName) ?: return null
        val prefix = "${accessTokenVerifier.scheme} "
        if (headerValue.startsWith(prefix, ignoreCase = true)) {
            return headerValue.substring(prefix.length).trim().takeIf { it.isNotBlank() }
        }
        return null
    }

    sealed class VerificationResult {
        data class Authenticated(val claims: AccessTokenClaims) : VerificationResult()
        data class Failed(val exception: SecurityException) : VerificationResult()
    }
}
