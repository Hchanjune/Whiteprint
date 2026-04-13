package org.whiteprint.platform.core.security.provider

import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.AccessTokenProfile
import org.whiteprint.platform.core.security.model.RefreshToken
import org.whiteprint.platform.core.security.model.RefreshTokenProfile
import org.whiteprint.platform.core.security.policy.TokenPolicy

interface TokenProvider {
    val policy: TokenPolicy
    fun generateAccessToken(profile: AccessTokenProfile): AccessToken
    fun generateRefreshToken(profile: RefreshTokenProfile): RefreshToken
}