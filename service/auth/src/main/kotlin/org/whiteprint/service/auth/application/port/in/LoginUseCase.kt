package org.whiteprint.service.auth.application.port.`in`

import io.github.hchanjune.omk.core.OperationResult

interface LoginUseCase {
    fun handle(command: LoginUseCase): OperationResult<LoginResult>
}