package org.whiteprint.platform.adapter.persistence.servlet.guards

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration
import kotlin.jvm.java

class StarterConfigGuardFilter: AutoConfigurationImportFilter {

    private val excluded = setOf(
        DataSourceAutoConfiguration::class.java.name,
        HibernateJpaAutoConfiguration::class.java.name,
        TransactionAutoConfiguration::class.java.name,
        JdbcTemplateAutoConfiguration::class.java.name,
        MongoAutoConfiguration::class.java.name,
    )

    override fun match(
        autoConfigurationClasses: Array<out String?>,
        autoConfigurationMetadata: AutoConfigurationMetadata
    ): BooleanArray {
        return BooleanArray(autoConfigurationClasses.size) { i ->
            val name = autoConfigurationClasses[i]
            name != null && !excluded.contains(name)
        }
    }
}