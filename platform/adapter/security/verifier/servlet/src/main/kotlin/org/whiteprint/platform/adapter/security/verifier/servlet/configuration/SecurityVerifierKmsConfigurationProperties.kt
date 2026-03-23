package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.security.verifier.kms")
data class SecurityVerifierKmsConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    ) {

    data class DataSourceProperties(
        var host: String = "",
        var port: Int = 8200,
        var password: String = ""
    )

}