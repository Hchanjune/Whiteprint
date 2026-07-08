package org.whiteprint.platform.adapter.security.verifier.reactive.security

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.policy.SecurityException
import org.whiteprint.platform.core.security.policy.SecurityPolicy
import reactor.core.publisher.Mono

object SecurityContextSupport {

    fun currentClaims(): Mono<AccessTokenClaims> =
        ReactiveSecurityContextHolder.getContext()
            .map { ctx ->
                (ctx.authentication as? VerifiedUser)?.claims
                    ?: throw SecurityException(SecurityPolicy.TOKEN_NOT_FOUND)
            }

    fun currentSubject(): Mono<String> = currentClaims().map { it.subject }

    suspend fun awaitCurrentClaims(): AccessTokenClaims = currentClaims().awaitSingle()

    suspend fun awaitCurrentClaimsOrNull(): AccessTokenClaims? = currentClaims().awaitSingleOrNull()

    suspend fun awaitCurrentSubject(): String = currentSubject().awaitSingle()

    suspend fun awaitCurrentSubjectOrNull(): String? = currentSubject().awaitSingle().takeUnless { it.isEmpty() }
}
