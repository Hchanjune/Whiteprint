package org.whiteprint.platform.adapter.persistence.reactive.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.persistence-reactive")
data class PersistenceConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.REACTIVE_MONGO,
) {

    enum class InfrastructureImplementation {
        REACTIVE_MONGO
    }

}