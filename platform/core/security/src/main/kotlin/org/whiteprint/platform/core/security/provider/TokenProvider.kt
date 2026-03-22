package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenClaims
import org.whiteprint.platform.core.security.model.AccessTokenSigningKey
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenClaims
import org.whiteprint.platform.core.security.model.RefreshTokenKey

interface TokenProvider {
    fun generateAccessToken(claims: AccessTokenClaims, key: AccessTokenSigningKey): AccessToken
    fun generateRefreshToken(claims: RefreshTokenClaims, key: RefreshTokenKey): RefreshToken
}