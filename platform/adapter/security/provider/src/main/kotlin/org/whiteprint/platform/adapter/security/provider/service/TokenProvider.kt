package org.whiteprint.platform.adapter.security.provider.service

import org.whiteprint.platform.adapter.security.provider.model.AccessTokenSigningKey
import org.whiteprint.platform.adapter.security.provider.model.RefreshToken
import org.whiteprint.platform.adapter.security.provider.model.RefreshTokenClaims
import org.whiteprint.platform.adapter.security.provider.model.RefreshTokenKey
import org.whiteprint.platform.adapter.security.verifier.model.AccessToken
import org.whiteprint.platform.adapter.security.verifier.model.AccessTokenClaims

interface TokenProvider {
    fun generateAccessToken(claims: AccessTokenClaims, key: AccessTokenSigningKey): AccessToken
    fun generateRefreshToken(claims: RefreshTokenClaims, key: RefreshTokenKey): RefreshToken
}