package org.whiteprint.platform.core.security.policy

data class TokenPolicy (
    val accessTokenPolicy: AccessTokenPolicy,
    val refreshTokenPolicy: RefreshTokenPolicy
) {

    data class AccessTokenPolicy(
        val issuer: String,
        val expirationSeconds: Long,
    )

    data class RefreshTokenPolicy (
        val issuer: String,
        val expirationSeconds: Long,
        val cookieHeader: String
    )

}