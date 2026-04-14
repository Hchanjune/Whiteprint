package org.whiteprint.service.auth.application.port.`in`.logout

import org.whiteprint.platform.core.security.model.RefreshToken
import java.time.Instant

data class LogoutCommand(
    val accessTokenId: String,
    val accessTokenExpiresAt: Instant,
    val subject: String,
    val refreshToken: RefreshToken,
)