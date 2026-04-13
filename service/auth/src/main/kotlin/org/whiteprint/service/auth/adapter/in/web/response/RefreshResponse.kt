package org.whiteprint.service.auth.adapter.`in`.web.response

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
