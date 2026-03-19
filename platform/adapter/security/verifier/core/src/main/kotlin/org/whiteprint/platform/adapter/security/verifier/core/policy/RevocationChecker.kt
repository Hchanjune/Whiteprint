package org.whiteprint.platform.adapter.security.verifier.core.policy

import java.time.Instant

interface RevocationChecker {
    fun getLastRevokedAt(identifier: String): Instant?
}