package org.whiteprint.sample.auth.adapter.`in`.web.response

import java.time.Instant

data class SignupResponse(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val signedUpAt: Instant,
)
