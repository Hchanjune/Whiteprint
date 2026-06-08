package org.whiteprint.sample.auth.application.port.`in`.logout

import io.github.hchanjune.omk.core.OperationResult

interface LogoutUseCase {
    fun handle(command: LogoutCommand): OperationResult<LogoutResult>
}