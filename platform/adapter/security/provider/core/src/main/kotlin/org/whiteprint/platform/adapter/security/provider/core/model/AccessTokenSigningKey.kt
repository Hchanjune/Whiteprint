package org.whiteprint.platform.adapter.security.provider.core.model

import java.security.PrivateKey

data class AccessTokenSigningKey(
    val keyId: String,
    val signingKey: PrivateKey
)