package org.whiteprint.platform.core.security.model

import java.security.PrivateKey

data class AccessTokenSigningKey(
    val keyId: String,
    val signingKey: PrivateKey
)