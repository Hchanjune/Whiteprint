package org.whiteprint.service.auth.application.port.`in`

import org.whiteprint.platform.core.security.model.RefreshToken

data class RefreshCommand(
    val refreshToken: RefreshToken
)