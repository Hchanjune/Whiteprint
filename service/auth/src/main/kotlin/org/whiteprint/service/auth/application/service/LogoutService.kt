package org.whiteprint.service.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service
import org.whiteprint.platform.core.security.policy.RevocationReason
import org.whiteprint.platform.core.security.provider.TokenRevoker
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.service.auth.application.port.`in`.logout.LogoutCommand
import org.whiteprint.service.auth.application.port.`in`.logout.LogoutResult
import org.whiteprint.service.auth.application.port.`in`.logout.LogoutUseCase
import java.time.Duration
import java.time.Instant

@ManagedService
@Service
class LogoutService(
    private val revoker: TokenRevoker,
    private val refreshTokenVerifier: RefreshTokenVerifier
): LogoutUseCase {

    @ManagedOperation("Logout")
    override fun handle(command: LogoutCommand): OperationResult<LogoutResult> = Operations {
        val now = Instant.now()

        val refreshTokenClaims = refreshTokenVerifier.verifyOrThrow(command.refreshToken)

        revoker.revokeToken(
            tokenId = command.accessTokenId,
            reason = RevocationReason.LOGOUT,
            duration = Duration.between(now, command.accessTokenExpiresAt)
        )
        revoker.revokeToken(
            tokenId = refreshTokenClaims.tokenId,
            reason = RevocationReason.LOGOUT,
            duration = Duration.between(now, refreshTokenClaims.expiresAt)
        )

        message = "Logout successful."

        LogoutResult(result = true)
    }

}