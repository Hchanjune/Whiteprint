package org.whiteprint.platform.core.security.model

data class AccessTokenSigningKeyMetadata(
    val keyAlias: String,
    val keyVersion: String,
    val algorithm: String,
)