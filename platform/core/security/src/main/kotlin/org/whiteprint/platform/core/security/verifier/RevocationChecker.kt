package org.whiteprint.platform.core.security.verifier

import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshTokenClaims

interface RevocationChecker {
    fun assertNotRevoked(claims: AccessTokenClaims)
    fun assertNotRevoked(claims: RefreshTokenClaims)
}