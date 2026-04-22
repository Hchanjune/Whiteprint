package org.whiteprint.service.user.command.application.service

import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.whiteprint.service.user.command.application.port.`in`.command.SignupCommand
import org.whiteprint.service.user.command.application.port.`in`.usecase.SignupUseCase
import org.whiteprint.service.user.command.application.port.out.persistence.UserRepository
import org.whiteprint.service.user.command.domain.user.aggregate.UserAggregate

@Service
@ManagedService
class UserCommandService(
    private val userRepository: UserRepository,
): SignupUseCase {

    @ManagedOperation(useCase = "Signup.General")
    @Transactional
    override fun handle(command: SignupCommand.General) = Operations {
        UserAggregate.signup(
            email = command.email,
            password = command.password,
            username = command.username,
            locale = command.locale,
            timeZone = command.timeZone,
            gender = command.gender,
            phone = command.phone,
            birthDate = command.birthDate,
        ).also { aggregate ->
            userRepository.create(aggregate)
        }
    }

    @ManagedOperation(useCase = "Signup.Oauth")
    @Transactional
    override fun handle(command: SignupCommand.Oauth) = Operations {
        UserAggregate.signupOauth(
            email = command.email,
            provider = command.provider,
            providerSubject = command.providerSubject,
            isEmailVerified = true,
            username = command.username,
            locale = command.locale,
            timeZone = command.timeZone,
            gender = command.gender,
            phone = command.phone,
            birthDate = command.birthDate,
        ).also { aggregate ->
            userRepository.create(aggregate)
        }
    }


}