package com.hc.user.command.application.port.`in`

import com.hc.user.command.domain.model.user.UserAggregate
import io.github.hchanjune.omk.core.models.OperationResult

interface SignupUseCase {
    fun handle(command: SignupCommand.General): OperationResult<UserAggregate>
    fun handle(command: SignupCommand.Oauth): OperationResult<UserAggregate>
}