package org.whiteprint.service.auth.application.port.`in`

import io.github.hchanjune.omk.core.OperationResult

interface SignupUseCase {

    fun handle(command: SignupCommand): OperationResult<SignupResult>

}