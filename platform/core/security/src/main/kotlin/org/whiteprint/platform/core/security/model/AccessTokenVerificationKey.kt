package org.whiteprint.platform.core.security.model

import java.security.PublicKey

data class AccessTokenVerificationKey(
    val keyId: String,
    val verifyKey: PublicKey
)