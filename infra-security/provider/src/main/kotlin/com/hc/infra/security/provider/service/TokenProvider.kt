package com.hc.infra.security.provider.service

import com.hc.infra.security.provider.model.AccessTokenSigningKey
import com.hc.infra.security.provider.model.RefreshToken
import com.hc.infra.security.provider.model.RefreshTokenClaims
import com.hc.infra.security.provider.model.RefreshTokenKey
import com.hc.infra.security.verifier.model.AccessToken
import com.hc.infra.security.verifier.model.AccessTokenClaims

interface TokenProvider {
    fun generateAccessToken(claims: AccessTokenClaims, key: AccessTokenSigningKey): AccessToken
    fun generateRefreshToken(claims: RefreshTokenClaims, key: RefreshTokenKey): RefreshToken
}