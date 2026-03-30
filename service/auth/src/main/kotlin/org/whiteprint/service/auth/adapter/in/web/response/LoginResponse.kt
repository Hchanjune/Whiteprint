package org.whiteprint.service.auth.adapter.`in`.web.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
)