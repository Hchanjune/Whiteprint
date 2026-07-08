package org.whiteprint.platform.adapter.cache.servlet.configuration

import io.lettuce.core.api.StatefulConnection
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.whiteprint.platform.core.cache.operation.AtomicOperations
import org.whiteprint.platform.core.cache.operation.BatchOperations
import org.whiteprint.platform.core.cache.operation.ListOperations
import org.whiteprint.platform.core.cache.operation.SetOperations
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.provider.CacheProvider
import org.whiteprint.platform.adapter.cache.servlet.aspect.CacheEvictAspect
import org.whiteprint.platform.adapter.cache.servlet.aspect.CachedAspect
import org.whiteprint.platform.adapter.cache.servlet.aspect.DeduplicatedAspect
import org.whiteprint.platform.adapter.cache.servlet.aspect.IdempotentAspect
import org.whiteprint.platform.adapter.cache.servlet.aspect.RateLimitedAspect
import org.whiteprint.platform.infra.cache.redis.operation.RedisAtomicOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisBatchOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisListOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisSetOperations
import org.whiteprint.platform.infra.cache.redis.operation.RedisValueOperations
import org.whiteprint.platform.infra.cache.redis.provider.RedisCacheProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import java.time.Duration

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties::class)
class CacheConfiguration(
    private val properties: CacheConfigurationProperties,
) {

    @Primary
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
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

    @Bean
    fun redisConnectionValidator(
        redisConnectionFactory: RedisConnectionFactory
    ) = SmartInitializingSingleton {

        val logger = LoggerFactory.getLogger("RedisConnectionValidator")

        try {
            redisConnectionFactory.connection.use { conn ->
                val result = conn.ping()

                require(result == "PONG") {
                    "Redis ping failed: $result"
                }

                logger.info(
                    "Redis connection validation succeeded (host={}, port={}, db={})",
                    properties.datasource.host,
                    properties.datasource.port,
                    properties.datasource.database
                )
            }
        } catch (e: Exception) {
            throw IllegalStateException("Redis connection validation failed", e)
        }
    }

    @Primary
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            this.keySerializer = RedisSerializer.string()
            this.hashKeySerializer = RedisSerializer.string()
            this.valueSerializer = RedisSerializer.json()
            this.hashValueSerializer = RedisSerializer.json()
            this.afterPropertiesSet()
        }
    }

    @Bean
    fun valueOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): ValueOperations =
        RedisValueOperations(redisTemplate)

    @Bean
    fun atomicOperation(
        redisTemplate: RedisTemplate<String, Any>,
    ): AtomicOperations =
        RedisAtomicOperations(redisTemplate)

    @Bean
    fun batchOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): BatchOperations =
        RedisBatchOperations(redisTemplate)

    @Bean
    fun listOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): ListOperations =
        RedisListOperations(redisTemplate)

    @Bean
    fun setOperations(
        redisTemplate: RedisTemplate<String, Any>,
    ): SetOperations =
        RedisSetOperations(redisTemplate)

    @Bean
    fun cacheProvider(
        valueOperations: ValueOperations,
        atomicOperation: AtomicOperations,
        batchOperations: BatchOperations,
        listOperations: ListOperations,
        setOperations: SetOperations,
    ): CacheProvider = RedisCacheProvider(
        value = valueOperations,
        atomic = atomicOperation,
        batch = batchOperations,
        list = listOperations,
        set = setOperations,
    )

    @Bean
    fun cachedAspect(cacheProvider: CacheProvider) = CachedAspect(cacheProvider)

    @Bean
    fun idempotentAspect(cacheProvider: CacheProvider) = IdempotentAspect(cacheProvider)

    @Bean
    fun deduplicatedAspect(cacheProvider: CacheProvider) = DeduplicatedAspect(cacheProvider)

    @Bean
    fun rateLimitedAspect(cacheProvider: CacheProvider) = RateLimitedAspect(cacheProvider)

    @Bean
    fun cacheEvictAspect(cacheProvider: CacheProvider) = CacheEvictAspect(cacheProvider)

}