package org.whiteprint.sample.auth.application.port.`in`.logout

import org.whiteprint.platform.core.security.model.RefreshToken
import java.time.Instant

sealed class LogoutCommand {
    abstract val accessTokenId: String
    abstract val accessTokenExpiresAt: Instant
    abstract val subject: String

    data class CurrentDevice(
        override val accessTokenId: String,
        override val accessTokenExpiresAt: Instant,
        override val subject: String,
        val refreshToken: RefreshToken,
    ) : LogoutCommand()

    data class AllDevices(
        override val accessTokenId: String,
        override val accessTokenExpiresAt: Instant,
        override val subject: String,
    ) : LogoutCommand()
}