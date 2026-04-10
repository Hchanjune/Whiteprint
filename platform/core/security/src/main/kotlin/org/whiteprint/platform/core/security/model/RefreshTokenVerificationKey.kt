package org.whiteprint.platform.core.security.model

import java.security.PublicKey

data class RefreshTokenVerificationKey(
    val keyAlias: String,
    val keyVersion: String?,
    val verifyKey: PublicKey
)