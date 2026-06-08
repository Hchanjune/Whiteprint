package org.whiteprint.sample.auth.adapter.`in`.web.request

data class SignupRequest(
    val username: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val passwordCheck: String
)