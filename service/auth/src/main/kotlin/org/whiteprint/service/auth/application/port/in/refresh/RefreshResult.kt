package org.whiteprint.service.auth.application.port.`in`.refresh

import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.RefreshToken

data class RefreshResult(
    val accessToken: AccessToken,
    val accessTokenExpiration: Long,
    val refreshToken: RefreshToken,
    val refreshTokenExpiration: Long,
    val refreshTokenCookieHeader: String
)