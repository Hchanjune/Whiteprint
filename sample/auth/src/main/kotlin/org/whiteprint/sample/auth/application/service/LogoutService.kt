package org.whiteprint.sample.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service
import org.whiteprint.platform.core.security.policy.RevocationPolicy
import org.whiteprint.platform.core.security.policy.RevocationReason
import org.whiteprint.platform.core.security.verifier.TokenRevoker
import org.whiteprint.platform.core.security.verifier.RefreshTokenVerifier
import org.whiteprint.sample.auth.application.port.`in`.logout.LogoutCommand
import org.whiteprint.sample.auth.application.port.`in`.logout.LogoutResult
import org.whiteprint.sample.auth.application.port.`in`.logout.LogoutUseCase
import java.time.Duration
import java.time.Instant

@ManagedService
@Service
class LogoutService(
    private val revoker: TokenRevoker,
    private val revocation: RevocationPolicy,
    private val refreshTokenVerifier: RefreshTokenVerifier,
): LogoutUseCase {

    @ManagedOperation("Logout")
    override fun handle(command: LogoutCommand): OperationResult<LogoutResult> = Operations {
        val now = Instant.now()

        when (command) {
            is LogoutCommand.CurrentDevice -> {
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
            }
            is LogoutCommand.AllDevices -> {
                revoker.revokeAccount(
                    subject = command.subject,
                    reason = RevocationReason.LOGOUT,
                    duration = revocation.accountRevocationDuration
                )
            }
        }

        LogoutResult(result = true).apply {
            message = "Logout successful."
        }
    }

}