package org.whiteprint.platform.adapter.security.verifier.core.service

import org.whiteprint.platform.adapter.security.verifier.core.model.AccessToken
import org.whiteprint.platform.adapter.security.verifier.core.model.AccessTokenClaims


interface AccessTokenVerifier {
    fun verifyOrThrow(token: AccessToken): AccessTokenClaims
}