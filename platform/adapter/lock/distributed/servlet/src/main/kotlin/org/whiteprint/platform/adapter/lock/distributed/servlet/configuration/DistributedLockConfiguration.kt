package org.whiteprint.platform.adapter.lock.distributed.servlet.configuration

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
import org.whiteprint.platform.adapter.lock.distributed.servlet.aspect.DistributedLockAspect
import org.whiteprint.platform.core.lock.operation.DistributedLockOperations
import org.whiteprint.platform.core.lock.provider.DistributedLockOwnerProvider
import org.whiteprint.platform.infra.cache.redis.operation.RedisDistributedLockOperations
import org.whiteprint.platform.infra.cache.redis.provider.DefaultDistributedLockOwnerProvider
import java.time.Duration

@Configuration
@EnableConfigurationProperties(LockConfigurationProperties::class)
class DistributedLockConfiguration(
    private val properties: LockConfigurationProperties,
) {

    @Bean("lockRedisConnectionFactory")
    fun lockRedisConnectionFactory(): RedisConnectionFactory {
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

    @Bean("lockRedisTemplate")
    fun lockRedisTemplate(
        @Qualifier("lockRedisConnectionFactory")
        lockRedisConnectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, Any> =
        RedisTemplate<String, Any>().apply {
            this.connectionFactory = lockRedisConnectionFactory
            this.keySerializer = RedisSerializer.string()
            this.valueSerializer = RedisSerializer.string()
            this.afterPropertiesSet()
        }

    @Bean
    fun distributedLockOwnerProvider(): DistributedLockOwnerProvider =
        DefaultDistributedLockOwnerProvider()

    @Bean
    fun distributedLockOperations(
        @Qualifier("lockRedisTemplate")
        lockRedisTemplate: RedisTemplate<String, Any>,
        distributedLockOwnerProvider: DistributedLockOwnerProvider,
    ): DistributedLockOperations =
        RedisDistributedLockOperations(lockRedisTemplate, distributedLockOwnerProvider.provideOwner())

    @Bean
    fun distributedLockAspect(
        lockOperations: DistributedLockOperations,
    ): DistributedLockAspect =
        DistributedLockAspect(lockOperations)

}
