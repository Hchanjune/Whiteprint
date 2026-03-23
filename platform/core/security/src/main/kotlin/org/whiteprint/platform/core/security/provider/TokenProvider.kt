package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenClaims

interface TokenProvider {
    fun generateAccessToken(claims: AccessTokenClaims): AccessToken
    fun generateRefreshToken(claims: RefreshTokenClaims): RefreshToken
}