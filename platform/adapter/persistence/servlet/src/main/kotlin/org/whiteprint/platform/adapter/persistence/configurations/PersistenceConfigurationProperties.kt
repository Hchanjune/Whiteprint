package org.whiteprint.platform.adapter.persistence.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.persistence")
data class PersistenceConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.JPA,
) {

    enum class InfrastructureImplementation {
        JPA,
        MONGO
    }

}