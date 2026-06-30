package org.whiteprint.platform.adapter.event.outbox.configuration

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class JpaAutoConfigurationGuard : EnvironmentPostProcessor {

    companion object {
        private val JPA_EXCLUSIONS = listOf(
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
            "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
        )
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val inboxImpl = environment.getProperty("adapter.event.inbox.infrastructure-implementation")
        val outboxImpl = environment.getProperty("adapter.event.outbox.infrastructure-implementation")
        val persistenceImpl = environment.getProperty("adapter.persistence.infrastructure-implementation")

        val anyJpa = listOf(inboxImpl, outboxImpl, persistenceImpl).any { it?.lowercase() == "jpa" }
        if (anyJpa) return

        val existing = environment.getProperty("spring.autoconfigure.exclude") ?: ""
        val existingList = existing.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val merged = (existingList + JPA_EXCLUSIONS).distinct().joinToString(",")

        environment.propertySources.addFirst(
            MapPropertySource("platformJpaGuard", mapOf("spring.autoconfigure.exclude" to merged))
        )
    }
}
