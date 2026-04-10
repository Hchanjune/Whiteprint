package org.whiteprint.platform.core.security.model

data class RefreshTokenProfile(
    override val subject: String,
    override val audience: Set<String>,
): TokenProfile
