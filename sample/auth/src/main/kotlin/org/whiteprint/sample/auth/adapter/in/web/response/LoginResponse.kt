package org.whiteprint.sample.auth.adapter.`in`.web.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val failedAttempts: Int
)