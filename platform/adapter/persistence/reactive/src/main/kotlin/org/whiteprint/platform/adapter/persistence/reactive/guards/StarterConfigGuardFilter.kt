package org.whiteprint.platform.adapter.persistence.reactive.guards

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveRepositoriesAutoConfiguration
import org.springframework.boot.mongodb.autoconfigure.MongoReactiveAutoConfiguration

class StarterConfigGuardFilter : AutoConfigurationImportFilter {

    private val excluded = setOf(
        MongoReactiveAutoConfiguration::class.java.name,
        DataMongoReactiveRepositoriesAutoConfiguration::class.java.name,
    )

    override fun match(
        autoConfigurationClasses: Array<out String?>,
        autoConfigurationMetadata: AutoConfigurationMetadata,
    ): BooleanArray {
        return BooleanArray(autoConfigurationClasses.size) { i ->
            val name = autoConfigurationClasses[i]
            name != null && !excluded.contains(name)
        }
    }

}