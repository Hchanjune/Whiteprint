package org.whiteprint.service.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.provider.TokenProvider
import org.whiteprint.service.auth.application.port.`in`.LoginCommand
import org.whiteprint.service.auth.application.port.`in`.LoginResult
import org.whiteprint.service.auth.application.port.`in`.LoginUseCase
import org.whiteprint.service.auth.application.port.out.AccountRepository
import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicyException
import org.whiteprint.service.auth.domain.accounts.vo.AccountIdentifier
import org.whiteprint.service.auth.domain.accounts.vo.RawPassword

@ManagedService
@Service
class LoginService(
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val repository: AccountRepository
): LoginUseCase {

    @ManagedOperation(useCase = "Login")
    @Transactional
    override fun handle(command: LoginCommand): OperationResult<LoginResult> = Operations {

        val accountAggregate = try {
            repository.findByIdentifierOrThrow(
                object : AccountIdentifier {
                    override val value: String
                        get() = command.identifier
                }
            )
        } catch (_: AccountPolicyException) {
            throw AccountPolicyException(AccountPolicy.LOGIN_FAILURE)
        }

        val loginResult = accountAggregate.login(
            rawPassword = RawPassword(command.rawPassword),
            passwordEncoder = passwordEncoder
        )

        if (loginResult) {
            val accessToken = tokenProvider.generateAccessToken(
                AccessTokenProfile(
                    subject = accountAggregate.id.toString(),
                    audience = emptySet(),
                    permissions = emptySet(),
                )
            )

            val refreshToken = tokenProvider.generateRefreshToken(
                RefreshTokenProfile(
                    subject = accountAggregate.id.toString(),
                    audience = emptySet(),
                )
            )

            LoginResult(
                accessToken = accessToken,
                refreshToken = refreshToken,
                failedAttempts = accountAggregate.failedAttempts,
            ).apply {
                repository.update(accountAggregate)
                message = "Login successful."
            }
        } else {
            LoginResult(
                accessToken = AccessToken("LoginFailure"),
                refreshToken = RefreshToken("LoginFailure"),
                failedAttempts = accountAggregate.failedAttempts,
            ).apply {
                repository.update(accountAggregate)
                message = "Login failed."
            }
        }

    }

}