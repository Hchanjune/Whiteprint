package org.whiteprint.platform.adapter.security.provider.core.service

import org.whiteprint.platform.adapter.security.provider.core.model.AccessTokenSigningKey
import org.whiteprint.platform.adapter.security.provider.core.model.RefreshToken
import org.whiteprint.platform.adapter.security.provider.core.model.RefreshTokenClaims
import org.whiteprint.platform.adapter.security.provider.core.model.RefreshTokenKey
import org.whiteprint.platform.adapter.security.verifier.core.model.AccessToken
import org.whiteprint.platform.adapter.security.verifier.core.model.AccessTokenClaims


interface TokenProvider {
    fun generateAccessToken(claims: AccessTokenClaims, key: AccessTokenSigningKey): AccessToken
    fun generateRefreshToken(claims: RefreshTokenClaims, key: RefreshTokenKey): RefreshToken
}