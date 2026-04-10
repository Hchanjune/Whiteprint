package org.whiteprint.platform.core.security.model

data class AccessTokenProfile(
    override val subject: String,
    override val audience: Set<String>,
    val permissions: Set<String>,
): TokenProfile