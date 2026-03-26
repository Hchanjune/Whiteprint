package org.whiteprint.platform.core.security.model

import javax.crypto.SecretKey

data class RefreshTokenKey(
    val keyId: String,
    val secretKey: SecretKey,
    val algorithm: String
)