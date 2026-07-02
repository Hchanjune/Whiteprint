package org.whiteprint.sample.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.servlet.Operations
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.whiteprint.platform.core.messaging.outbox.EventProducer
import org.whiteprint.sample.auth.application.port.`in`.signup.SignupCommand
import org.whiteprint.sample.auth.application.port.`in`.signup.SignupResult
import org.whiteprint.sample.auth.application.port.`in`.signup.SignupUseCase
import org.whiteprint.sample.auth.application.port.out.event.AccountCreatedEvent
import org.whiteprint.sample.auth.application.port.out.persistence.AccountRepository
import org.whiteprint.sample.auth.domain.accounts.aggregate.AccountAggregate
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.sample.auth.domain.accounts.policy.AccountPolicyException
import org.whiteprint.sample.auth.domain.accounts.vo.Email
import org.whiteprint.sample.auth.domain.accounts.vo.PasswordHash
import org.whiteprint.sample.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.sample.auth.domain.accounts.vo.RawPassword
import org.whiteprint.sample.auth.domain.accounts.vo.RawPasswordCheck
import org.whiteprint.sample.auth.domain.accounts.vo.Username

@ManagedService
@Service
class SignupService(
    private val passwordEncoder: PasswordEncoder,
    private val repository: AccountRepository,
    private val eventProducer: EventProducer
): SignupUseCase {

    @ManagedOperation(useCase = "Signup")
    @Transactional
    override fun handle(command: SignupCommand): OperationResult<SignupResult> = Operations {
        val username = Username(command.username)
        val email = Email(command.email)
        val phoneNumber = PhoneNumber(command.phoneNumber)
        val rawPassword = RawPassword(command.rawPassword)
        val rawPasswordCheck = RawPasswordCheck(command.rawPasswordCheck)

        if (repository.existsByUsername(username)) {
            throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_USERNAME_DUPLICATED,
                attributes = mapOf("input" to username.value)
            )
        }
        if (repository.existsByEmail(email)) {
            throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_EMAIL_DUPLICATED,
                attributes = mapOf("input" to email.value)
            )
        }
        if (repository.existsByPhoneNumber(phoneNumber)) {
            throw AccountPolicyException(
                policy = AccountPolicy.ACCOUNT_PHONE_NUMBER_DUPLICATED,
                attributes = mapOf("input" to phoneNumber.value)
            )
        }
        if (rawPassword.value != rawPasswordCheck.value) {
            throw AccountPolicyException(
                policy = AccountPolicy.PASSWORD_MISS_MATCH
            )
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(rawPassword.value)) {
            "Password encoding failed and returned null"
        }

        val passwordHash = PasswordHash(encodedPassword)

        val accountAggregate = AccountAggregate.signup(
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            passwordHash = passwordHash
        ).also {
            repository.create(it)
        }

        SignupResult(
            id = accountAggregate.id,
            username = accountAggregate.username,
            email = accountAggregate.email,
            phoneNumber = accountAggregate.phoneNumber,
            signedUpAt = accountAggregate.insertedAt
        ).also {
            message = "Successfully signed up"
            eventProducer.produce(
                AccountCreatedEvent(
                    accountId = accountAggregate.id,
                )
            )
        }
    }


}