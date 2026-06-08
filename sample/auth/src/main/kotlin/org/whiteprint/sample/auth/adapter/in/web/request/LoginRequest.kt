package org.whiteprint.sample.auth.adapter.`in`.web.request

data class LoginRequest(
    val identifier: String,
    val password: String
)