package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.whiteprint.platform.core.kms.model.KeyType

@ConfigurationProperties(prefix = "adapter.security.provider.kms")
data class SecurityProviderKmsConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var accessTokenKeyPolicy: AccessTokenKeyPolicy = AccessTokenKeyPolicy(),
    var refreshTokenKeyPolicy: RefreshTokenKeyPolicy = RefreshTokenKeyPolicy()
) {

    data class DataSourceProperties(
        var host: String = "",
        var port: Int = 8200,
        var password: String = ""
    )

    data class AccessTokenKeyPolicy(
        var keyAlias: String = "access-token-sig",

        var rotationIntervalSeconds: Long = 2592000,
        var overlapSeconds: Long = 86400,
        var algorithm: KeyType = KeyType.RSA_2048
    )

    data class RefreshTokenKeyPolicy(
        var keyAlias: String = "refresh-token-sig",
        var expirationSeconds: Long = 604800,

        var rotationIntervalSeconds: Long = 2592000,
        var overlapSeconds: Long = 86400,
        var algorithm: KeyType = KeyType.HMAC_SHA256
    )

}