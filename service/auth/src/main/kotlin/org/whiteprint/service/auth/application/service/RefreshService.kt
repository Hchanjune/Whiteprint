package org.whiteprint.service.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.policy.RevocationReason
import org.whiteprint.platform.core.security.provider.TokenProvider
import org.whiteprint.platform.core.security.verifier.AccountTokenStatusManager
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.platform.core.security.verifier.TokenRevoker
import org.whiteprint.service.auth.application.port.`in`.refresh.RefreshCommand
import org.whiteprint.service.auth.application.port.`in`.refresh.RefreshResult
import org.whiteprint.service.auth.application.port.`in`.refresh.RefreshUseCase
import org.whiteprint.service.auth.application.port.out.persistence.AccountRepository
import java.time.Duration
import java.time.Instant

@ManagedService
@Service
class RefreshService(
    private val tokenVerifier: RefreshTokenVerifier,
    private val tokenProvider: TokenProvider,
    private val tokenRevoker: TokenRevoker,
    private val tokenStatusManager: AccountTokenStatusManager,
    private val repository: AccountRepository
): RefreshUseCase {

    @ManagedOperation(useCase = "Refresh")
    @Transactional(readOnly = true)
    override fun handle(command: RefreshCommand): OperationResult<RefreshResult> = Operations {
        val claims = tokenVerifier.verifyOrThrow(command.refreshToken)

        tokenRevoker.revokeToken(
            tokenId = claims.tokenId,
            reason = RevocationReason.REFRESH_ROTATION,
            duration = Duration.between(Instant.now(), claims.expiresAt)
        )

        val isForceUpdate = tokenStatusManager.checkForceUpdate(claims.subject, claims.issuedAt.toEpochMilli())

        val aggregate = if (isForceUpdate) repository.findByIdOrThrow(claims.subject.toLong()) else null

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
        ).apply {
            message = "Successfully refreshed tokens"
        }
    }

}