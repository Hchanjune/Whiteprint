package com.hc.core.jwt.verifier

import com.hc.core.jwt.model.RefreshToken
import com.hc.core.jwt.model.RefreshTokenClaims

interface RefreshTokenVerifier {
    fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims
}