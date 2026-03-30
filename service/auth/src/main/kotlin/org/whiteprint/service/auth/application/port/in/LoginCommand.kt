package org.whiteprint.service.auth.application.port.`in`

data class LoginCommand(
    val username: String,
    val password: String
)