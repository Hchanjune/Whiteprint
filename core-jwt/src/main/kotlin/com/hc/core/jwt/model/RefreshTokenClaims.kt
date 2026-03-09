package com.hc.core.jwt.model

import java.time.Instant

data class RefreshTokenClaims (
    val subject: TokenSubjects,
    val issuer: String,
    val audience: Set<String>,
    val issuedAt: Instant = Instant.now(),
    val expiresAt: Instant,

    val userId: String,
)