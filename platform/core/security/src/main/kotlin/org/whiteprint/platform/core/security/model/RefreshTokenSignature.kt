package org.whiteprint.platform.core.security.model

@JvmInline
value class RefreshTokenSignature(
    override val signature: String
): TokenSignature