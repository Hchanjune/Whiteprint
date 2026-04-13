package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.security.provider.token")
data class SecurityProviderConfigurationProperties (
    var accessTokenPolicy: AccessTokenPolicy = AccessTokenPolicy(),
    var refreshTokenPolicy: RefreshTokenPolicy = RefreshTokenPolicy()
) {

    data class AccessTokenPolicy(
        var issuer: String = "Sample",
        var expirationSeconds: Long = 3600,
    )

    data class RefreshTokenPolicy(
        var issuer: String = "Sample",
        var expirationSeconds: Long = 3600,
        var cookieHeader: String = "refresh"
    )

}