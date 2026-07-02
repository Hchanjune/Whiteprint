package org.whiteprint.platform.adapter.event.inbox.guards

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment

class StarterConfigGuardFilter : AutoConfigurationImportFilter, EnvironmentAware {

    private var environment: Environment? = null

    private val JPA_EXCLUSIONS = setOf(
        DataSourceAutoConfiguration::class.java.name,
        DataSourceTransactionManagerAutoConfiguration::class.java.name,
        JdbcTemplateAutoConfiguration::class.java.name,
        HibernateJpaAutoConfiguration::class.java.name,
    )

    private val MONGO_SERVLET_EXCLUSIONS = setOf(
        MongoAutoConfiguration::class.java.name,
        DataMongoAutoConfiguration::class.java.name,
        DataMongoRepositoriesAutoConfiguration::class.java.name,
    )

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun match(
        autoConfigurationClasses: Array<out String?>,
        autoConfigurationMetadata: AutoConfigurationMetadata,
    ): BooleanArray {
        val env = environment
        val inboxImpl = env?.getProperty("adapter.event.inbox.infrastructure-implementation")
        val outboxImpl = env?.getProperty("adapter.event.outbox.infrastructure-implementation")
        val persistenceImpl = env?.getProperty("adapter.persistence.infrastructure-implementation")
        val webAppType = env?.getProperty("spring.main.web-application-type") ?: "servlet"

        val anyJpa = listOf(inboxImpl, outboxImpl, persistenceImpl).any { it?.lowercase() == "jpa" }
        val needsServletMongo = listOf(inboxImpl, outboxImpl).any { it?.lowercase() == "mongo" }
            && webAppType.lowercase() != "reactive"

        val excluded = buildSet {
            if (!anyJpa) addAll(JPA_EXCLUSIONS)
            if (!needsServletMongo) addAll(MONGO_SERVLET_EXCLUSIONS)
        }

        return BooleanArray(autoConfigurationClasses.size) { i ->
            val name = autoConfigurationClasses[i]
            name == null || !excluded.contains(name)
        }
    }
}
