package org.whiteprint.platform.adapter.security.provider.core.model

import java.time.Instant

data class RefreshTokenClaims(
    val subject: String,
    val issuer: String,
    val audience: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant,
)