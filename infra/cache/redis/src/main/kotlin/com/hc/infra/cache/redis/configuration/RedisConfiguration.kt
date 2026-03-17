package com.hc.infra.cache.redis.configuration

import com.hc.core.cache.operation.AtomicOperations
import com.hc.core.cache.operation.BatchOperations
import com.hc.core.cache.operation.DistributedLockOperations
import com.hc.core.cache.operation.ListOperations
import com.hc.core.cache.operation.SetOperations
import com.hc.core.cache.operation.ValueOperations
import com.hc.core.cache.provider.CacheProvider
import com.hc.core.cache.provider.DistributedLockOwnerProvider
import com.hc.infra.cache.redis.operation.RedisAtomicOperations
import com.hc.infra.cache.redis.operation.RedisBatchOperations
import com.hc.infra.cache.redis.operation.RedisDistributedLockOperations
import com.hc.infra.cache.redis.operation.RedisListOperations
import com.hc.infra.cache.redis.operation.RedisSetOperations
import com.hc.infra.cache.redis.operation.RedisValueOperations
import com.hc.infra.cache.redis.provider.DefaultDistributedLockOwnerProvider
import com.hc.infra.cache.redis.provider.RedisCacheProvider
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
@EnableConfigurationProperties(_root_ide_package_.com.hc.infra.cache.redis.configuration.RedisConfigurationProperties::class)
class RedisConfiguration(
    private val redisProperties: com.hc.infra.cache.redis.configuration.RedisConfigurationProperties,
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
    fun distributedLockOwnerProvider(): DistributedLockOwnerProvider =
        _root_ide_package_.com.hc.infra.cache.redis.provider.DefaultDistributedLockOwnerProvider()

    @Bean
    fun valueOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): ValueOperations = _root_ide_package_.com.hc.infra.cache.redis.operation.RedisValueOperations(redisTemplate)

    @Bean
    fun atomicOperation(
        redisTemplate: RedisTemplate<String, Any>,
    ): AtomicOperations = _root_ide_package_.com.hc.infra.cache.redis.operation.RedisAtomicOperations(redisTemplate)

    @Bean
    fun batchOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): BatchOperations = _root_ide_package_.com.hc.infra.cache.redis.operation.RedisBatchOperations(redisTemplate)

    @Bean
    fun listOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): ListOperations = _root_ide_package_.com.hc.infra.cache.redis.operation.RedisListOperations(redisTemplate)

    @Bean
    fun setOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): SetOperations = _root_ide_package_.com.hc.infra.cache.redis.operation.RedisSetOperations(redisTemplate)

    @Bean
    fun distributedLockOperations(
        redisTemplate: RedisTemplate<String, Any>,
        distributedLockOwnerProvider: DistributedLockOwnerProvider,
    ): DistributedLockOperations = _root_ide_package_.com.hc.infra.cache.redis.operation.RedisDistributedLockOperations(
        redisTemplate,
        distributedLockOwnerProvider.provideOwner()
    )

    @Bean
    fun cacheProvider(
        valueOperations: ValueOperations,
        atomicOperation: AtomicOperations,
        batchOperations: BatchOperations,
        listOperations: ListOperations,
        setOperations: SetOperations,
        distributedLockOperations: DistributedLockOperations
    ): CacheProvider = _root_ide_package_.com.hc.infra.cache.redis.provider.RedisCacheProvider(
        value = valueOperations,
        atomic = atomicOperation,
        batch = batchOperations,
        list = listOperations,
        set = setOperations,
        lock = distributedLockOperations
    )

}