package org.whiteprint.platform.core.security.model

data class RefreshTokenSigningKeyMetadata(
    val keyAlias: String,
    val keyVersion: String,
    val algorithm: String
)