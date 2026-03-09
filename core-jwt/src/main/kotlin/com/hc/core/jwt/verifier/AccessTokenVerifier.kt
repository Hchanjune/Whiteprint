package com.hc.core.jwt.verifier

import com.hc.core.jwt.model.AccessToken
import com.hc.core.jwt.model.AccessTokenClaims

interface AccessTokenVerifier {
    fun verifyOrThrow(token: AccessToken): AccessTokenClaims
}