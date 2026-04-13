package org.whiteprint.platform.core.security.model

@JvmInline
value class AccessTokenSignature(
    override val signature: String
): TokenSignature