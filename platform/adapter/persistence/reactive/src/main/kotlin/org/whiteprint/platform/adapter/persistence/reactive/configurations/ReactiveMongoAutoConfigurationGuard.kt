package org.whiteprint.platform.adapter.persistence.reactive.configurations

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class ReactiveMongoAutoConfigurationGuard : EnvironmentPostProcessor {

    companion object {
        private val SYNC_MONGO_EXCLUSIONS = listOf(
            "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration",
            "org.springframework.boot.mongodb.autoconfigure.MongoDatabaseFactoryConfiguration",
        )
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val persistenceReactiveImpl = environment.getProperty("adapter.persistence-reactive.infrastructure-implementation")
        if (persistenceReactiveImpl?.lowercase() != "reactive_mongo") return

        val existing = environment.getProperty("spring.autoconfigure.exclude") ?: ""
        val existingList = existing.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val merged = (existingList + SYNC_MONGO_EXCLUSIONS).distinct().joinToString(",")

        environment.propertySources.addFirst(
            MapPropertySource("platformReactiveMongoGuard", mapOf("spring.autoconfigure.exclude" to merged))
        )
    }
}
