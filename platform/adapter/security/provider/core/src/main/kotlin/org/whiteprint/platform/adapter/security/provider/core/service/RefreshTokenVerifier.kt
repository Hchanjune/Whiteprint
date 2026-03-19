package org.whiteprint.platform.adapter.security.provider.core.service

import org.whiteprint.platform.adapter.security.provider.core.model.RefreshToken
import org.whiteprint.platform.adapter.security.provider.core.model.RefreshTokenClaims

interface RefreshTokenVerifier {
    fun verifyOrThrow(token: RefreshToken): RefreshTokenClaims
}