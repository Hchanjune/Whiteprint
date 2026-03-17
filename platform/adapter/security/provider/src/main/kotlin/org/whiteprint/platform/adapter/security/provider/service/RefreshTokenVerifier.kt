package org.whiteprint.platform.adapter.security.provider.service

import org.whiteprint.platform.adapter.security.provider.model.RefreshToken
import org.whiteprint.platform.adapter.security.provider.model.RefreshTokenClaims

interface RefreshTokenVerifier {
    fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims
}