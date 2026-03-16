package com.hc.infra.redis.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "redis")
data class RedisConfigurationProperties (
    var host: String = "",
    var port: Int = 6379,
    var password: String? = null,
    var timeout: Timeout = Timeout(),
) {
    data class Timeout (
        var commandTimeoutMillis: Long = 2000,
        var shutdownTimeoutMillis: Long = 100,
    )
}