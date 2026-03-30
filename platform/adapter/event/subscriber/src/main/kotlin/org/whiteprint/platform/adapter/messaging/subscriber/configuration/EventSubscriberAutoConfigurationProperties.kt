package org.whiteprint.platform.adapter.messaging.subscriber.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.messaging.subscriber")
data class EventSubscriberAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.KAFKA,
) {
    enum class InfrastructureImplementation {
        KAFKA
    }
}
