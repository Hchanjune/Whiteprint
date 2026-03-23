package org.whiteprint.platform.core.security.policy

data class TokenPolicy (
    val accessTokenPolicy: AccessTokenPolicy = AccessTokenPolicy(),
    val refreshTokenPolicy: RefreshTokenPolicy = RefreshTokenPolicy()
) {

    data class AccessTokenPolicy(
        val issuer: String = "Not Implemented",
        val expirationSeconds: Long = 0L
    )

    data class RefreshTokenPolicy (
        val issuer: String = "Not Implemented",
        val expirationSeconds: Long = 0L
    )

}