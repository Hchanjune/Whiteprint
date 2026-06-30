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
        if (permittedPathProvider.isPermitted(exchange.request)) {
            return chain.filter(exchange)
        }

        return Mono.fromCallable { verifyToken(exchange) }
            .subscribeOn(Schedulers.boundedElastic())
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
                        exchange.attributes[SECURITY_EXCEPTION_KEY] = result.exception
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
