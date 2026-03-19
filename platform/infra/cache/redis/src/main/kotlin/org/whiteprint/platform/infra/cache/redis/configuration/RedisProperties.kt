package org.whiteprint.platform.infra.cache.redis.configuration

data class RedisProperties(
    val host: String = "",
    val port: Int = 6379,
    val password: String? = null,
    val database: Int = 0,

    val enabled: Boolean = true,
    val maxActive: Int = 8,
    val maxIdle: Int = 8,
    val minIdle: Int = 0,
    val maxWaitMillis: Long = -1,

    val commandTimeoutMillis: Long = 2000,
    val shutdownTimeoutMillis: Long = 100,
)