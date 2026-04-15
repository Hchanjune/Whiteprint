package org.whiteprint.platform.core.security.model

import java.time.Instant

interface TokenClaims {
    val tokenId: String
    val subject: String
    val issuer: String
    val audience: Set<String>
    val issuedAt: Instant
    val expiresAt: Instant
    val permissions: Set<String>
}