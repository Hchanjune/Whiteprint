package org.whiteprint.platform.adapter.security.provider.model

import javax.crypto.SecretKey

data class RefreshTokenKey(
    val keyId: String,
    val secretKey: SecretKey,
)