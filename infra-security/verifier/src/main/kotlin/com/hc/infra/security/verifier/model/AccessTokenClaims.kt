package com.hc.infra.security.verifier.model

import java.time.Instant

data class AccessTokenClaims(
    val subject: String,
    val issuer: String,
    val audience: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val authorities: Set<String>,
)