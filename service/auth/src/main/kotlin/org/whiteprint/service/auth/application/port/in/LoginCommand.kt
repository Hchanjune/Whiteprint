package org.whiteprint.service.auth.application.port.`in`

data class LoginCommand(
    val identifier: String,
    val password: String
)