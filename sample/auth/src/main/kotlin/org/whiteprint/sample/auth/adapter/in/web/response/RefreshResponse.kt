package org.whiteprint.sample.auth.adapter.`in`.web.response

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
