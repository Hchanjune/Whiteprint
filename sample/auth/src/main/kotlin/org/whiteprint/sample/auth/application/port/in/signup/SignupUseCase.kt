package org.whiteprint.sample.auth.application.port.`in`.signup

import io.github.hchanjune.omk.core.OperationResult

interface SignupUseCase {

    fun handle(command: SignupCommand): OperationResult<SignupResult>

}