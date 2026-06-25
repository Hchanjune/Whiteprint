package org.whiteprint.platform.adapter.event.inbox.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.inbox")
data class EventInboxAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.JPA
) {
    enum class InfrastructureImplementation {
        JPA,
        MONGODB_REACTIVE
    }
}
