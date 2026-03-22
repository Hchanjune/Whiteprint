package org.whiteprint.platform.core.security.verifier

import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims


interface AccessTokenVerifier {
    fun verifyOrThrow(token: AccessToken): AccessTokenClaims
}