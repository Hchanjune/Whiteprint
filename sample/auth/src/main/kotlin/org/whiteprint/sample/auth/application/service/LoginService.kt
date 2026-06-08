package org.whiteprint.sample.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.platform.core.messaging.outbox.EventProducer
import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.provider.TokenProvider
import org.whiteprint.sample.auth.application.port.`in`.login.LoginCommand
import org.whiteprint.sample.auth.application.port.`in`.login.LoginResult
import org.whiteprint.sample.auth.application.port.`in`.login.LoginUseCase
import org.whiteprint.sample.auth.application.port.out.event.LoginCommittedEvent
import org.whiteprint.sample.auth.application.port.out.persistence.AccountRepository
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicyException
import org.whiteprint.sample.auth.domain.accounts.vo.AccountIdentifier
import org.whiteprint.sample.auth.domain.accounts.vo.RawPassword
import org.whiteprint.sample.auth.domain.audit.vo.LoginLogStatus

@ManagedService
@Service
class LoginService(
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val repository: AccountRepository,
    private val eventProducer: EventProducer
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
                accessTokenExpiration = tokenProvider.policy.accessTokenPolicy.expirationSeconds,
                refreshToken = refreshToken,
                refreshTokenExpiration = tokenProvider.policy.refreshTokenPolicy.expirationSeconds,
                refreshTokenCookieHeader = tokenProvider.policy.refreshTokenPolicy.cookieHeader,
                failedAttempts = accountAggregate.failedAttempts,
            ).apply {
                repository.update(accountAggregate)
                message = "Login successful."
                eventProducer.produce(
                    LoginCommittedEvent(
                        status = LoginLogStatus.SUCCESS,
                        attemptedIdentifier = command.identifier,
                        ip = command.clientContext.ip,
                        client = command.clientContext.type,
                        platform = command.clientContext.platform,
                    )
                )
            }
        } else {
            LoginResult(
                accessToken = AccessToken("LoginFailure"),
                accessTokenExpiration = 0,
                refreshToken = RefreshToken("LoginFailure"),
                refreshTokenExpiration = 0,
                refreshTokenCookieHeader = tokenProvider.policy.refreshTokenPolicy.cookieHeader,
                failedAttempts = accountAggregate.failedAttempts,
            ).apply {
                repository.update(accountAggregate)
                message = "Login failed."
                eventProducer.produce(
                    LoginCommittedEvent(
                        status = LoginLogStatus.FAIL,
                        attemptedIdentifier = command.identifier,
                        ip = command.clientContext.ip,
                        client = command.clientContext.type,
                        platform = command.clientContext.platform,
                    )
                )
            }
        }

    }

}