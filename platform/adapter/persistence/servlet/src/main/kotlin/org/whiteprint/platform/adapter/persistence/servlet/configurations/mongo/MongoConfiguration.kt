package org.whiteprint.platform.adapter.persistence.servlet.configurations.mongo

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.connection.ConnectionPoolSettings
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.whiteprint.platform.infra.persistence.mongo.servlet.repository.OptimizedMongoRepository
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import java.util.concurrent.TimeUnit

@Configuration
@ConditionalOnProperty(prefix = "adapter.persistence", name = ["infrastructureImplementation"], havingValue = "MONGO")
@EnableConfigurationProperties(MongoConfigurationProperties::class)
@EnableMongoRepositories(
    basePackages = ["org.whiteprint"],
    repositoryBaseClass = OptimizedMongoRepository::class,
)
class MongoConfiguration(
    private val properties: MongoConfigurationProperties,
) : AbstractMongoClientConfiguration() {

    override fun getDatabaseName(): String = properties.datasource.database

    @Bean
    override fun mongoClient(): MongoClient {
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
