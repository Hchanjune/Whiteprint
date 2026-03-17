package org.whiteprint.platform.adapter.security.provider.service

import com.hc.infra.security.provider.model.RefreshToken
import com.hc.infra.security.provider.model.RefreshTokenClaims

interface RefreshTokenVerifier {
    fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims
}