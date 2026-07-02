package org.whiteprint.platform.adapter.event.subscriber.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.subscriber")
data class EventSubscriberAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.KAFKA,
    var claimTimeoutMillis: Long = 300_000L,
) {
    enum class InfrastructureImplementation {
        KAFKA
    }
}
