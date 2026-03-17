package com.hc.infra.web.servlet.security

import com.hc.infra.security.verifier.policy.RevocationChecker
import java.time.Instant

class RevocationCheckerImpl: RevocationChecker {
    override fun getLastRevokedAt(identifier: String): Instant? {
        TODO("Not yet implemented")
    }
}