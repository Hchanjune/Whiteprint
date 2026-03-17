package com.hc.infra.security.verifier.service

import com.hc.infra.security.verifier.model.AccessToken
import com.hc.infra.security.verifier.model.AccessTokenClaims

interface AccessTokenVerifier {
    fun verifyOrThrow(token: AccessToken): AccessTokenClaims
}