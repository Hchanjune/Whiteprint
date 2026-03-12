package com.hc.infra.web.servlet.security

import com.hc.core.jwt.verifier.RevocationChecker
import java.time.Instant

class CommonTokenRevocationChecker: RevocationChecker {
    override fun getLastRevokedAt(identifier: String): Instant? {
        TODO("Not yet implemented")
    }
}