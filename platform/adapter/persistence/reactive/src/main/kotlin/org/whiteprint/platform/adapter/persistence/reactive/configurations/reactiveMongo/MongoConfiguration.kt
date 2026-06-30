package org.whiteprint.platform.adapter.persistence.reactive.configurations.reactiveMongo

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.whiteprint.platform.infra.persistence.mongo.reactive.repository.OptimizedReactiveMongoRepository
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(MongoConfigurationProperties::class)
@EnableReactiveMongoRepositories(
    basePackages = ["org.whiteprint"],
    repositoryBaseClass = OptimizedReactiveMongoRepository::class,
)
class MongoConfiguration(
    private val properties: MongoConfigurationProperties,
) : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName(): String = properties.datasource.database

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        val datasource = properties.datasource
        val pool = properties.pool

        val poolSettings = ConnectionPoolSettings.builder()
            .maxSize(pool.maxSize)
            .minSize(pool.minSize)
            .maxWaitTime(pool.maxWaitMillis, TimeUnit.MILLISECONDS)
            .maxConnectionIdleTime(pool.maxConnectionIdleTimeMillis, TimeUnit.MILLISECONDS)
            .maxConnectionLifeTime(pool.maxConnectionLifeTimeMillis, TimeUnit.MILLISECONDS)
            .build()

        val settingsBuilder = MongoClientSettings.builder()
            .applyToClusterSettings { it.hosts(listOf(ServerAddress(datasource.host, datasource.port))) }
            .applyToConnectionPoolSettings { it.applySettings(poolSettings) }

        if (datasource.username != null) {
            settingsBuilder.credential(
                MongoCredential.createCredential(
                    datasource.username!!,
                    datasource.authDatabase,
                    datasource.password?.toCharArray() ?: charArrayOf(),
                )
            )
        }

        return MongoClients.create(settingsBuilder.build())
    }

}
