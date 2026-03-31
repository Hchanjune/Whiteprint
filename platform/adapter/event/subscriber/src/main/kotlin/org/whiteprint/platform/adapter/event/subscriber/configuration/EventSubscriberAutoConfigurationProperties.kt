package org.whiteprint.platform.adapter.event.subscriber.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.subscriber")
data class EventSubscriberAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.KAFKA,
) {
    enum class InfrastructureImplementation {
        KAFKA
    }
}
