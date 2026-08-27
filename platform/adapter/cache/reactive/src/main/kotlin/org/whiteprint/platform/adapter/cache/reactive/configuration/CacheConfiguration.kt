package org.whiteprint.platform.adapter.cache.reactive.configuration

import io.github.hchanjune.omk.core.provider.SpanIdProvider
import io.lettuce.core.api.StatefulConnection
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.whiteprint.platform.infra.serializer.jackson.JacksonRedisSerializers
import org.whiteprint.platform.core.cache.operation.ReactiveAtomicOperations
import org.whiteprint.platform.core.cache.operation.ReactiveBatchOperations
import org.whiteprint.platform.core.cache.operation.ReactiveListOperations
import org.whiteprint.platform.core.cache.operation.ReactiveSetOperations
import org.whiteprint.platform.core.cache.operation.ReactiveValueOperations
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import org.whiteprint.platform.adapter.cache.reactive.aspect.CacheEvictAspect
import org.whiteprint.platform.adapter.cache.common.support.CacheKeyContractValidator
import org.whiteprint.platform.adapter.cache.reactive.aspect.CachedAspect
import org.whiteprint.platform.adapter.cache.reactive.aspect.DeduplicatedAspect
import org.whiteprint.platform.adapter.cache.reactive.aspect.IdempotentAspect
import org.whiteprint.platform.adapter.cache.reactive.aspect.RateLimitedAspect
import org.whiteprint.platform.infra.cache.redis.reactive.operation.RedisReactiveAtomicOperations
import org.whiteprint.platform.infra.cache.redis.reactive.operation.RedisReactiveBatchOperations
import org.whiteprint.platform.infra.cache.redis.reactive.operation.RedisReactiveListOperations
import org.whiteprint.platform.infra.cache.redis.reactive.operation.RedisReactiveSetOperations
import org.whiteprint.platform.infra.cache.redis.reactive.operation.RedisReactiveValueOperations
import org.whiteprint.platform.infra.cache.redis.reactive.provider.RedisReactiveCacheProvider
import java.time.Duration

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties::class)
class CacheConfiguration(
    private val properties: CacheConfigurationProperties,
) {

    @Primary
    @Bean
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
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
    fun reactiveRedisConnectionValidator(
        connectionFactory: ReactiveRedisConnectionFactory
    ) = SmartInitializingSingleton {
        val logger = LoggerFactory.getLogger("RedisReactiveConnectionValidator")

        val result = connectionFactory.reactiveConnection.ping().block()

        require(result == "PONG") { "Redis ping failed: $result" }

        logger.info(
            "Redis reactive connection validation succeeded (host={}, port={}, db={})",
            properties.datasource.host,
            properties.datasource.port,
            properties.datasource.database
        )
    }

    @Primary
    @Bean
    fun reactiveRedisTemplate(connectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val context = RedisSerializationContext.newSerializationContext<String, Any>(RedisSerializer.string())
            .value(JacksonRedisSerializers.json())
            .hashValue(JacksonRedisSerializers.json())
            .build()
        return ReactiveRedisTemplate(connectionFactory, context)
    }

    @Bean
    fun reactiveValueOperations(redisTemplate: ReactiveRedisTemplate<String, Any>): ReactiveValueOperations =
        RedisReactiveValueOperations(redisTemplate)

    @Bean
    fun reactiveAtomicOperations(redisTemplate: ReactiveRedisTemplate<String, Any>): ReactiveAtomicOperations =
        RedisReactiveAtomicOperations(redisTemplate)

    @Bean
    fun reactiveBatchOperations(redisTemplate: ReactiveRedisTemplate<String, Any>): ReactiveBatchOperations =
        RedisReactiveBatchOperations(redisTemplate)

    @Bean
    fun reactiveListOperations(redisTemplate: ReactiveRedisTemplate<String, Any>): ReactiveListOperations =
        RedisReactiveListOperations(redisTemplate)

    @Bean
    fun reactiveSetOperations(redisTemplate: ReactiveRedisTemplate<String, Any>): ReactiveSetOperations =
        RedisReactiveSetOperations(redisTemplate)

    @Bean
    fun reactiveCacheProvider(
        valueOperations: ReactiveValueOperations,
        atomicOperations: ReactiveAtomicOperations,
        batchOperations: ReactiveBatchOperations,
        listOperations: ReactiveListOperations,
        setOperations: ReactiveSetOperations,
    ): ReactiveCacheProvider = RedisReactiveCacheProvider(
        value = valueOperations,
        atomic = atomicOperations,
        batch = batchOperations,
        list = listOperations,
        set = setOperations,
    )

    @Bean
    fun cachedAspect(cacheProvider: ReactiveCacheProvider, spanIdProvider: SpanIdProvider) = CachedAspect(cacheProvider, spanIdProvider)

    @Bean
    fun idempotentAspect(cacheProvider: ReactiveCacheProvider, spanIdProvider: SpanIdProvider) = IdempotentAspect(cacheProvider, spanIdProvider)

    @Bean
    fun deduplicatedAspect(cacheProvider: ReactiveCacheProvider, spanIdProvider: SpanIdProvider) = DeduplicatedAspect(cacheProvider, spanIdProvider)

    @Bean
    fun rateLimitedAspect(cacheProvider: ReactiveCacheProvider, spanIdProvider: SpanIdProvider) = RateLimitedAspect(cacheProvider, spanIdProvider)

    @Bean
    fun cacheEvictAspect(cacheProvider: ReactiveCacheProvider, spanIdProvider: SpanIdProvider) = CacheEvictAspect(cacheProvider, spanIdProvider)

    /**
     * `@Cached` 와 `@CacheEvict` 의 키 구성이 어긋나면 기동을 실패시킨다.
     * 불일치는 예외 없이 "무효화만 안 걸리는" 형태로 나타나서 운영에서야 드러난다.
     */
    @Bean
    fun cacheKeyContractValidator(applicationContext: ApplicationContext) =
        CacheKeyContractValidator(applicationContext)

}
