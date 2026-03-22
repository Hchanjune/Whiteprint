package org.whiteprint.platform.core.security.model

import java.time.Instant

data class RefreshTokenClaims(
    val subject: String,
    val issuer: String,
    val audience: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant,
)