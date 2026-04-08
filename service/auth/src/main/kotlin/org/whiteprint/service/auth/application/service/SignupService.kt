package org.whiteprint.service.auth.application.service

import io.github.hchanjune.omk.core.OperationResult
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.whiteprint.service.auth.application.port.`in`.SignupCommand
import org.whiteprint.service.auth.application.port.`in`.SignupResult
import org.whiteprint.service.auth.application.port.`in`.SignupUseCase
import org.whiteprint.service.auth.domain.accounts.aggregate.AccountAggregate
import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicy
import org.whiteprint.service.auth.domain.accounts.policy.AccountPolicyException
import org.whiteprint.service.auth.domain.accounts.vo.Email
import org.whiteprint.service.auth.domain.accounts.vo.PasswordHash
import org.whiteprint.service.auth.domain.accounts.vo.PhoneNumber
import org.whiteprint.service.auth.domain.accounts.vo.RawPassword
import org.whiteprint.service.auth.domain.accounts.vo.RawPasswordCheck
import org.whiteprint.service.auth.domain.accounts.vo.Username

@ManagedService
@Service
class SignupService(
    private val passwordEncoder: PasswordEncoder
): SignupUseCase {

    override fun handle(command: SignupCommand): OperationResult<SignupResult> = Operations {
        val username = Username(command.username)
        val email = Email(command.email)
        val phoneNumber = PhoneNumber(command.phoneNumber)
        val rawPassword = RawPassword(command.rawPassword)
        val rawPasswordCheck = RawPasswordCheck(command.rawPasswordCheck)

        if (rawPassword.value != rawPasswordCheck.value) {
            throw AccountPolicyException(
                policy = AccountPolicy.PASSWORD_MISS_MATCH
            )
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(rawPassword.value)) {
            "Password encoding failed and returned null"
        }

        println(encodedPassword)

        val passwordHash = PasswordHash(encodedPassword)

        AccountAggregate.signup(
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            passwordHash = passwordHash
        )

        SignupResult()
    }


}