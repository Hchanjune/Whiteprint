package org.whiteprint.platform.adapter.messaging.producer.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.messaging.producer")
data class EventPublisherAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.KAFKA,
) {
    enum class InfrastructureImplementation {
        KAFKA
    }
}
