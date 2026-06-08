package org.whiteprint.sample.auth.application.port.`in`.signup

data class SignupCommand (
    val username: String,
    val email: String,
    val phoneNumber: String,
    val rawPassword: String,
    val rawPasswordCheck: String,
)