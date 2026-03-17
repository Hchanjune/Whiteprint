package com.hc.infra.security.provider.model

import javax.crypto.SecretKey

data class RefreshTokenKey(
    val keyId: String,
    val secretKey: SecretKey,
)