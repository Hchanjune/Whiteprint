package com.hc.infra.security.verifier.policy

import java.time.Instant

interface RevocationChecker {
    fun getLastRevokedAt(identifier: String): Instant?
}