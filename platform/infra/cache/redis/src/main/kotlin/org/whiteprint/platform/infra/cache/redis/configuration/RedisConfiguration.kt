package org.whiteprint.platform.infra.cache.redis.configuration

import org.whiteprint.platform.core.cache.operation.AtomicOperations
import org.whiteprint.platform.core.cache.operation.BatchOperations
import org.whiteprint.platform.core.cache.operation.DistributedLockOperations
import org.whiteprint.platform.core.cache.operation.ListOperations
import org.whiteprint.platform.core.cache.operation.SetOperations
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.provider.CacheProvider
import org.whiteprint.platform.core.cache.provider.DistributedLockOwnerProvider
import org.whiteprint.platform.infra.cache.redis.operation.RedisAtomicOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisBatchOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisDistributedLockOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisListOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisSetOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisValueOperations
import org.whiteprint.platform.infra.cache.redis.provider.DefaultDistributedLockOwnerProvider
import org.whiteprint.platform.infra.cache.redis.provider.RedisCacheProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import java.time.Duration

@Configuration
@EnableConfigurationProperties(_root_ide_package_.org.whiteprint.platform.infra.cache.redis.configuration.RedisConfigurationProperties::class)
class RedisConfiguration(
    private val redisProperties: org.whiteprint.platform.infra.cache.redis.configuration.RedisConfigurationProperties,
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val serverConfig = RedisStandaloneConfiguration(
            redisProperties.host,
            redisProperties.port
        ).apply {
            password = RedisPassword.of(redisProperties.password)
        }

        val clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(redisProperties.timeout.commandTimeoutMillis))
            .shutdownTimeout(Duration.ofMillis(redisProperties.timeout.shutdownTimeoutMillis))
            .build()

        return LettuceConnectionFactory(serverConfig, clientConfig)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            this.keySerializer = RedisSerializer.string()
            this.hashKeySerializer = RedisSerializer.string()
            this.valueSerializer = RedisSerializer.json()
            this.hashValueSerializer = RedisSerializer.json()
            afterPropertiesSet()
        }
    }

    @Bean
    fun distributedLockOwnerProvider(): org.whiteprint.platform.core.cache.provider.DistributedLockOwnerProvider =
        _root_ide_package_.org.whiteprint.platform.infra.cache.redis.provider.DefaultDistributedLockOwnerProvider()

    @Bean
    fun valueOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): org.whiteprint.platform.core.cache.operation.ValueOperations =
        _root_ide_package_.org.whiteprint.platform.infra.cache.redis.operation.RedisValueOperations(redisTemplate)

    @Bean
    fun atomicOperation(
        redisTemplate: RedisTemplate<String, Any>,
    ): org.whiteprint.platform.core.cache.operation.AtomicOperations =
        _root_ide_package_.org.whiteprint.platform.infra.cache.redis.operation.RedisAtomicOperations(redisTemplate)

    @Bean
    fun batchOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): org.whiteprint.platform.core.cache.operation.BatchOperations =
        _root_ide_package_.org.whiteprint.platform.infra.cache.redis.operation.RedisBatchOperations(redisTemplate)

    @Bean
    fun listOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): org.whiteprint.platform.core.cache.operation.ListOperations =
        _root_ide_package_.org.whiteprint.platform.infra.cache.redis.operation.RedisListOperations(redisTemplate)

    @Bean
    fun setOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): org.whiteprint.platform.core.cache.operation.SetOperations =
        _root_ide_package_.org.whiteprint.platform.infra.cache.redis.operation.RedisSetOperations(redisTemplate)

    @Bean
    fun distributedLockOperations(
        redisTemplate: RedisTemplate<String, Any>,
        distributedLockOwnerProvider: org.whiteprint.platform.core.cache.provider.DistributedLockOwnerProvider,
    ): org.whiteprint.platform.core.cache.operation.DistributedLockOperations =
        _root_ide_package_.org.whiteprint.platform.infra.cache.redis.operation.RedisDistributedLockOperations(
            redisTemplate,
            distributedLockOwnerProvider.provideOwner()
        )

    @Bean
    fun cacheProvider(
        valueOperations: org.whiteprint.platform.core.cache.operation.ValueOperations,
        atomicOperation: org.whiteprint.platform.core.cache.operation.AtomicOperations,
        batchOperations: org.whiteprint.platform.core.cache.operation.BatchOperations,
        listOperations: org.whiteprint.platform.core.cache.operation.ListOperations,
        setOperations: org.whiteprint.platform.core.cache.operation.SetOperations,
        distributedLockOperations: org.whiteprint.platform.core.cache.operation.DistributedLockOperations
    ): org.whiteprint.platform.core.cache.provider.CacheProvider = _root_ide_package_.org.whiteprint.platform.infra.cache.redis.provider.RedisCacheProvider(
        value = valueOperations,
        atomic = atomicOperation,
        batch = batchOperations,
        list = listOperations,
        set = setOperations,
        lock = distributedLockOperations
    )

}