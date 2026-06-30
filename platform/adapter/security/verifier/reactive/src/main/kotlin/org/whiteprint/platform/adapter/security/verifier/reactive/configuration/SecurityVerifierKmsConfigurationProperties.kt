package org.whiteprint.platform.adapter.security.verifier.reactive.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.security.verifier.kms")
data class SecurityVerifierKmsConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var cache: CacheOptions = CacheOptions()
) {

    data class DataSourceProperties(
        var host: String = "",
        var port: Int = 8200,
        var password: String = "",
        var transitPath: String = "transit"
    )

    data class CacheOptions(
        val expiresAfterWriteMinutes: Long = 60,
        var maximumSize: Long = 1000
    )
}
