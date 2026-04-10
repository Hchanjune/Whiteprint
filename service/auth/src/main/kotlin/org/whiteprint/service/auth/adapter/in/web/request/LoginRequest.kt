package org.whiteprint.service.auth.adapter.`in`.web.request

data class LoginRequest(
    val identifier: String,
    val password: String
)