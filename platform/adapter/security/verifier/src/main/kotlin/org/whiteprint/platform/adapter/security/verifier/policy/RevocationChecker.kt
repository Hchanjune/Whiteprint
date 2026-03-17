package org.whiteprint.platform.adapter.security.verifier.policy

import java.time.Instant

interface RevocationChecker {
    fun getLastRevokedAt(identifier: String): Instant?
}