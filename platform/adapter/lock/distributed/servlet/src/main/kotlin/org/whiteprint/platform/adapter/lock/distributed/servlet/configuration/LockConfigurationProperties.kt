package org.whiteprint.platform.adapter.lock.distributed.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.lock.distributed.servlet")
data class LockConfigurationProperties(
    var datasource: DataSourceProperties = DataSourceProperties(),
    var pooling: Pooling = Pooling(),
    var timeout: Timeout = Timeout(),
) {
    data class DataSourceProperties(
        var host: String = "",
        var port: Int = 6379,
        var password: String? = null,
        var database: Int = 0,
    )

    data class Pooling(
        var enabled: Boolean = true,
        var maxActive: Int = 4,
        var maxIdle: Int = 4,
        var minIdle: Int = 0,
        var maxWaitMillis: Long = -1,
    )

    data class Timeout(
        var commandTimeoutMillis: Long = 1000,
        var shutdownTimeoutMillis: Long = 100,
    )
}
