package org.whiteprint.platform.adapter.web.servlet.security

import org.whiteprint.platform.adapter.security.verifier.policy.RevocationChecker
import java.time.Instant

class RevocationCheckerImpl: RevocationChecker {
    override fun getLastRevokedAt(identifier: String): Instant? {
        TODO("Not yet implemented")
    }
}