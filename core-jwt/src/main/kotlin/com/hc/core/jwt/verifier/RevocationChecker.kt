package com.hc.core.jwt.verifier

import java.time.Instant

interface RevocationChecker {
    fun getLastRevokedAt(identifier: String): Instant?
}