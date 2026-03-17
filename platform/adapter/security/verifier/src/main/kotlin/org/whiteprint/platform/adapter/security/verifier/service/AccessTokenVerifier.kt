package org.whiteprint.platform.adapter.security.verifier.service

import org.whiteprint.platform.adapter.security.verifier.model.AccessToken
import org.whiteprint.platform.adapter.security.verifier.model.AccessTokenClaims

interface AccessTokenVerifier {
    fun verifyOrThrow(token: AccessToken): AccessTokenClaims
}