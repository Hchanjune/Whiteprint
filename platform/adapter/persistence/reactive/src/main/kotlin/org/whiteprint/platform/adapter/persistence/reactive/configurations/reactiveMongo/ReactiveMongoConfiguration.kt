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
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(ReactiveMongoConfigurationProperties::class)
@Import(ReactiveMongoRepositoryRegistrar::class)
class ReactiveMongoConfiguration(
    private val properties: ReactiveMongoConfigurationProperties,
) {

    @Bean
    fun reactiveMongoClient(): MongoClient {
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

    @Bean
    fun reactiveMongoDatabaseFactory(client: MongoClient): ReactiveMongoDatabaseFactory =
        SimpleReactiveMongoDatabaseFactory(client, properties.datasource.database)

}
