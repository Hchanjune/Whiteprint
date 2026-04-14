package org.whiteprint.service.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.provider.TokenProvider
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.service.auth.application.port.`in`.refresh.RefreshCommand
import org.whiteprint.service.auth.application.port.`in`.refresh.RefreshResult
import org.whiteprint.service.auth.application.port.`in`.refresh.RefreshUseCase

@ManagedService
@Service
class RefreshService(
    private val tokenVerifier: RefreshTokenVerifier,
    private val tokenProvider: TokenProvider,
): RefreshUseCase {

    @ManagedOperation(useCase = "Refresh")
    override fun handle(command: RefreshCommand): OperationResult<RefreshResult> = Operations {
        val claims = tokenVerifier.verifyOrThrow(command.refreshToken)

        val accessToken = tokenProvider.generateAccessToken(
            AccessTokenProfile(
                subject = claims.subject,
                audience = emptySet(),
                permissions = emptySet(),
            )
        )

        val refreshToken = tokenProvider.generateRefreshToken(
            RefreshTokenProfile(
                subject = claims.subject,
                audience = emptySet(),
            )
        )

        RefreshResult(
            accessToken = accessToken,
            accessTokenExpiration = tokenProvider.policy.accessTokenPolicy.expirationSeconds,
            refreshToken = refreshToken,
            refreshTokenExpiration = tokenProvider.policy.refreshTokenPolicy.expirationSeconds,
            refreshTokenCookieHeader = tokenProvider.policy.refreshTokenPolicy.cookieHeader
        )
    }

}