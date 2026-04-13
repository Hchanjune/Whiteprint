package org.whiteprint.platform.core.security.model

data class RefreshTokenPayload(
    override val jti: String,
    override val sub: String,
    override val iss: String,
    override val aud: Set<String>,
    override val iat: Long,
    override val exp: Long
): TokenPayload
