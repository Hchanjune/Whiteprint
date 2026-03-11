package com.hc.core.jwt.model

import java.security.PublicKey

data class AccessTokenVerificationKey(
    val keyId: String,
    val value: PublicKey
)