package org.whiteprint.sample.auth.application.port.`in`.login

import io.github.hchanjune.omk.core.OperationResult

interface LoginUseCase {
    fun handle(command: LoginCommand): OperationResult<LoginResult>
}