package org.whiteprint.service.user.command.application.port.`in`

import io.github.hchanjune.omk.core.OperationResult
import org.whiteprint.service.user.command.domain.model.user.UserAggregate

interface SignupUseCase {
    fun handle(command: SignupCommand.General): OperationResult<UserAggregate>
    fun handle(command: SignupCommand.Oauth): OperationResult<UserAggregate>
}