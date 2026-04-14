package org.whiteprint.service.auth.application.port.`in`.login

data class LoginCommand(
    val identifier: String,
    val rawPassword: String
)