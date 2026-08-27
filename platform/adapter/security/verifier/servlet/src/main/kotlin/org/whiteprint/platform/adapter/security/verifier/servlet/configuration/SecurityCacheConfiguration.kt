package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import io.lettuce.core.api.StatefulConnection
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.whiteprint.platform.infra.serializer.jackson.JacksonRedisSerializers
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisValueOperations
import java.time.Duration

@Configuration
@EnableConfigurationProperties(SecurityCacheConfigurationProperties::class)
class SecurityCacheConfiguration(
    private val properties: SecurityCacheConfigurationProperties,
) {

    @Bean("securityCacheConnectionFactory")
    fun securityCacheConnectionFactory(): RedisConnectionFactory {
        val datasource = properties.datasource
        val timeout = properties.timeout
        val pooling = properties.pooling

        val serverConfig = RedisStandaloneConfiguration(datasource.host, datasource.port).apply {
            password = RedisPassword.of(datasource.password)
            database = datasource.database
        }

        val clientConfigBuilder = if (pooling.enabled) {
            val poolConfig = GenericObjectPoolConfig<StatefulConnection<*, *>>().apply {
                maxTotal = pooling.maxActive
                maxIdle = pooling.maxIdle
                minIdle = pooling.minIdle
                if (pooling.maxWaitMillis > 0) {
                    setMaxWait(Duration.ofMillis(pooling.maxWaitMillis))
                }
            }
            LettucePoolingClientConfiguration.builder().poolConfig(poolConfig)
        } else {
            LettuceClientConfiguration.builder()
        }

        val clientConfig = clientConfigBuilder
            .commandTimeout(Duration.ofMillis(timeout.commandTimeoutMillis))
            .shutdownTimeout(Duration.ofMillis(timeout.shutdownTimeoutMillis))
            .build()

        return LettuceConnectionFactory(serverConfig, clientConfig)
    }


    @Bean("securityCacheTemplate")
    fun securityCacheTemplate(
        @Qualifier("securityCacheConnectionFactory") connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = RedisSerializer.string()
            hashKeySerializer = RedisSerializer.string()
            valueSerializer = JacksonRedisSerializers.json()
            hashValueSerializer = JacksonRedisSerializers.json()
            afterPropertiesSet()
        }
    }

    @Bean("securityCacheValueOperations")
    fun securityCacheValueOperations(
        @Qualifier("securityCacheTemplate") cacheTemplate: RedisTemplate<String, Any>,
    ): ValueOperations =
        RedisValueOperations(cacheTemplate)

}