package org.whiteprint.platform.infra.cache.redis.configuration

import io.lettuce.core.api.StatefulConnection
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import java.time.Duration

class RedisFactory(
    private val property: RedisProperties
) {

    private val connectionFactory: RedisConnectionFactory by lazy {
        createConnectionFactory()
    }

    fun connectionFactory(): RedisConnectionFactory = connectionFactory

    fun redisTemplate(): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = RedisSerializer.string()
            hashKeySerializer = RedisSerializer.string()
            valueSerializer = RedisSerializer.json()
            hashValueSerializer = RedisSerializer.json()
            afterPropertiesSet()
        }
    }

    private fun createConnectionFactory(): RedisConnectionFactory {
        val serverConfig = RedisStandaloneConfiguration(property.host, property.port).apply {
            password = RedisPassword.of(property.password)
            database = property.database
        }

        val clientConfigBuilder = if (property.enabled) {
            val poolConfig = GenericObjectPoolConfig<StatefulConnection<*, *>>().apply {
                maxTotal = property.maxActive
                maxIdle = property.maxIdle
                minIdle = property.minIdle
                if (property.maxWaitMillis > 0) {
                    setMaxWait(Duration.ofMillis(property.maxWaitMillis))
                }
            }
            LettucePoolingClientConfiguration.builder().poolConfig(poolConfig)
        } else {
            LettuceClientConfiguration.builder()
        }

        val clientConfig = clientConfigBuilder
            .commandTimeout(Duration.ofMillis(property.commandTimeoutMillis))
            .shutdownTimeout(Duration.ofMillis(property.shutdownTimeoutMillis))
            .build()

        return LettuceConnectionFactory(serverConfig, clientConfig)
    }
}