package com.hc.core.jwt.model

import java.security.PrivateKey

data class AccessTokenSigningKey(
    val keyId: String,
    val value: PrivateKey
)
