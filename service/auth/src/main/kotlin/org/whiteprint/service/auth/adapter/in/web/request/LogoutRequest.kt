package org.whiteprint.service.auth.adapter.`in`.web.request

data class LogoutRequest(
    val refreshToken: String,
)