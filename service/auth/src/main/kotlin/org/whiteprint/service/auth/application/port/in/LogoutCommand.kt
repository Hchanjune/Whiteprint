package org.whiteprint.service.auth.application.port.`in`

import org.whiteprint.platform.core.security.model.AccessToken
import org.whiteprint.platform.core.security.model.RefreshToken

data class LogoutCommand(
    val accountId: Long,
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)