package org.whiteprint.service.auth.application.port.`in`

interface LogoutUseCase {
    fun handle(command: LogoutCommand)
}