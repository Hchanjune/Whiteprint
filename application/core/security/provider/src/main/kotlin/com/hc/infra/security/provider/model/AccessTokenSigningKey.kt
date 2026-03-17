package com.hc.infra.security.provider.model

import java.security.PrivateKey

data class AccessTokenSigningKey(
    val keyId: String,
    val signingKey: PrivateKey
)