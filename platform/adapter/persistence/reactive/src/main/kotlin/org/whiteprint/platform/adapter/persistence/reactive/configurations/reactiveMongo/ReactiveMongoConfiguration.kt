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
import org.springframework.data.mapping.model.FieldNamingStrategy
import org.springframework.data.mapping.model.Property
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy
import org.springframework.data.mapping.model.SimpleTypeHolder
import org.springframework.data.mongodb.MongoManagedTypes
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.BasicMongoPersistentProperty
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(ReactiveMongoConfigurationProperties::class)
@Import(ReactiveMongoRepositoryRegistrar::class)
class ReactiveMongoConfiguration(
    private val properties: ReactiveMongoConfigurationProperties,
) : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName(): String = properties.datasource.database

    override fun autoIndexCreation(): Boolean = properties.options.autoIndexCreation

    @Bean
    override fun mongoMappingContext(
        customConversions: MongoCustomConversions,
        managedTypes: MongoManagedTypes,
    ): MongoMappingContext {
        val ctx = if (properties.options.writeNullValues) WriteNullValuesMappingContext() else MongoMappingContext()
        ctx.setManagedTypes(managedTypes)
        ctx.setSimpleTypeHolder(customConversions.simpleTypeHolder)
        ctx.setFieldNamingStrategy(fieldNamingStrategy())
        ctx.setAutoIndexCreation(autoIndexCreation())
        return ctx
    }

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

        if (!datasource.username.isNullOrBlank()) {
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

private class WriteNullValuesMappingContext : MongoMappingContext() {
    private var namingStrategy: FieldNamingStrategy = PropertyNameFieldNamingStrategy.INSTANCE

    override fun setFieldNamingStrategy(strategy: FieldNamingStrategy?) {
        super.setFieldNamingStrategy(strategy)
        namingStrategy = strategy ?: PropertyNameFieldNamingStrategy.INSTANCE
    }

    override fun createPersistentProperty(
        property: Property,
        owner: MongoPersistentEntity<*>,
        simpleTypeHolder: SimpleTypeHolder,
    ): MongoPersistentProperty =
        object : BasicMongoPersistentProperty(property, owner, simpleTypeHolder, namingStrategy) {
            override fun writeNullValues(): Boolean = true
        }
}
