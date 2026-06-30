package org.whiteprint.platform.adapter.security.verifier.reactive.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.security.verifier.cache")
data class SecurityCacheConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var cachePrefix: String = "",
    var pooling: Pooling = Pooling(),
    var timeout: Timeout = Timeout(),
) {
    data class DataSourceProperties(
        var host: String = "",
        var port: Int = 6379,
        var password: String? = null,
        var database: Int = 0
    )

    data class Pooling(
        var enabled: Boolean = true,
        var maxActive: Int = 8,
        var maxIdle: Int = 8,
        var minIdle: Int = 0,
        var maxWaitMillis: Long = -1,
    )

    data class Timeout(
        var commandTimeoutMillis: Long = 2000,
        var shutdownTimeoutMillis: Long = 100,
    )
}
