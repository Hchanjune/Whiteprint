package org.whiteprint.platform.adapter.security.verifier.core.model

import java.security.PublicKey

data class AccessTokenVerificationKey(
    val keyId: String,
    val verifyKey: PublicKey
)