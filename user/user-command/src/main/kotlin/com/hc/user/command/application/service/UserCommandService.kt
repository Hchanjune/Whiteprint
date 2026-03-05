package com.hc.user.command.application.service

import com.hc.core.domain.identifier.TsidGenerator
import com.hc.user.command.application.port.`in`.SignupCommand
import com.hc.user.command.application.port.`in`.SignupUseCase
import com.hc.user.command.domain.model.user.User
import com.hc.user.command.domain.model.user.UserAggregate
import com.hc.user.command.domain.model.user.UserCredential
import com.hc.user.command.domain.model.user.UserProfile
import io.github.hchanjune.operationresult.core.Operations
import io.github.hchanjune.operationresult.core.annotations.OperationManaged
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@OperationManaged(operation = "UserCommand")
class UserCommandService(

): SignupUseCase {

    @OperationManaged(useCase = "Signup.General")
    @Transactional
    override fun handle(command: SignupCommand.General) = Operations {
        val user = User(
            id = TsidGenerator.generate(),
            email = command.email,
            lastLogin = null,
            isAccountLocked = true,
            isAccountAvailable = false,
            insertedAt = Instant.now(),
            updatedAt = Instant.now(),
            isDeleted = false,
            deletedAt = null,
            version = 0
        )
        val credential = UserCredential(
            id = TsidGenerator.generate(),
            passwordHash = command.password // Temporary Raw Password (Sample)
        )
        val profile = UserProfile(
            id = TsidGenerator.generate(),
            username = command.username,
            locale = command.locale,
            timeZone = command.timeZone,
            gender = command.gender,
            phone = command.phone,
            birthDate = command.birthDate,
        )
        val aggregate = UserAggregate.create(
            user = user,
            credential = credential,
            profile = profile
        )
        aggregate
    }

    @OperationManaged(useCase = "Signup.Oauth")
    @Transactional
    override fun handle(command: SignupCommand.Oauth) = Operations {
        val user = User(
            id = TsidGenerator.generate(),
            email = command.email,
            lastLogin = null,
            isAccountLocked = true,
            isAccountAvailable = false,
            insertedAt = Instant.now(),
            updatedAt = Instant.now(),
            isDeleted = false,
            deletedAt = null,
            version = 0
        )
        val credential = UserCredential(
            id = TsidGenerator.generate(),
            passwordHash = "OAUTH"
        )
        val profile = UserProfile(
            id = TsidGenerator.generate(),
            username = command.username,
            locale = command.locale,
            timeZone = command.timeZone,
            gender = command.gender,
            phone = command.phone,
            birthDate = command.birthDate,
        )
        val aggregate = UserAggregate.create(
            user = user,
            credential = credential,
            profile = profile
        )
        aggregate
    }


}