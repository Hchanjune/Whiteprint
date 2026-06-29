package org.whiteprint.platform.adapter.event.outbox.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.outbox")
data class EventOutboxAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.JPA
) {
    enum class InfrastructureImplementation {
        JPA,
        MONGO,
    }
}
