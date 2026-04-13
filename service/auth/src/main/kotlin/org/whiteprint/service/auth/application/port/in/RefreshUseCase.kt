package org.whiteprint.service.auth.application.port.`in`

import io.github.hchanjune.omk.core.OperationResult

interface RefreshUseCase {

    fun handle(command: RefreshCommand): OperationResult<RefreshResult>

}