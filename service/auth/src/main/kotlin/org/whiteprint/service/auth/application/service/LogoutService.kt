package org.whiteprint.service.auth.application.service

import io.github.hchanjune.omk.core.annotations.ManagedOperation
import io.github.hchanjune.omk.core.annotations.ManagedService
import org.springframework.stereotype.Service
import org.whiteprint.platform.core.security.provider.TokenRevoker
import org.whiteprint.service.auth.application.port.`in`.LogoutCommand
import org.whiteprint.service.auth.application.port.`in`.LogoutUseCase

@ManagedService
@Service
class LogoutService(

): LogoutUseCase {

    @ManagedOperation("Logout")
    override fun handle(command: LogoutCommand) {

        TODO("Not yet implemented")
    }

}