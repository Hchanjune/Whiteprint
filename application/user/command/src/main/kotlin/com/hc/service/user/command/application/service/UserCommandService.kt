package com.hc.service.user.command.application.service

import com.hc.user.command.application.port.`in`.SignupCommand
import com.hc.user.command.application.port.`in`.SignupUseCase
import com.hc.user.command.application.port.out.UserEventPublisher
import com.hc.user.command.application.port.out.UserRepository
import com.hc.user.command.domain.model.user.UserAggregate
import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import io.github.hchanjune.omk.webmvc.Operations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@ManagedService
class UserCommandService(
    private val userRepository: UserRepository,
    private val userEventPublisher: UserEventPublisher
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
            userEventPublisher.publish(aggregate)
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
            userEventPublisher.publish(aggregate)
        }
    }


}