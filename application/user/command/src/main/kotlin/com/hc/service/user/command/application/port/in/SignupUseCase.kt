package com.hc.service.user.command.application.port.`in`

import com.hc.user.command.domain.model.user.UserAggregate
import io.github.hchanjune.omk.core.OperationResult

interface SignupUseCase {
    fun handle(command: SignupCommand.General): OperationResult<UserAggregate>
    fun handle(command: SignupCommand.Oauth): OperationResult<UserAggregate>
}