package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.adapter.security.verifier.core.policy.RevocationChecker
import java.time.Instant

class RevocationCheckerImpl: RevocationChecker {
    override fun getLastRevokedAt(identifier: String): Instant? {
        TODO("Not yet implemented")
    }
}