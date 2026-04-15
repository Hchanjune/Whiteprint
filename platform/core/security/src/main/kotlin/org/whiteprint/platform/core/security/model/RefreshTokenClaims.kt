package org.whiteprint.platform.core.security.model

import java.time.Instant

data class RefreshTokenClaims(
    override val tokenId: String,
    override val subject: String,
    override val issuer: String,
    override val audience: Set<String>,
    override val issuedAt: Instant,
    override val expiresAt: Instant,
    override val permissions: Set<String>,
): TokenClaims