package org.whiteprint.service.auth.application.port.`in`

data class SignupCommand (
    val username: String,
    val email: String,
    val phoneNumber: String,
    val rawPassword: String,
    val rawPasswordCheck: String,
)