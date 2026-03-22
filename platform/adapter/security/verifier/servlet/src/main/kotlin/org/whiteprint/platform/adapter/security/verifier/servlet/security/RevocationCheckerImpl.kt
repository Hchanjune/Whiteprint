package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.verifier.RevocationChecker

class RevocationCheckerImpl(
    private val valueOperations: ValueOperations
): RevocationChecker {
    override fun assertNotRevoked(claims: AccessTokenClaims) {
        TODO("Not yet implemented")
    }

    override fun assertNotRevoked(claims: RefreshTokenClaims) {
        TODO("Not yet implemented")
    }
}