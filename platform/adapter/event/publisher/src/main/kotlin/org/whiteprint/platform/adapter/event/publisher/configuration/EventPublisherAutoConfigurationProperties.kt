package org.whiteprint.platform.adapter.event.publisher.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "adapter.event.publisher")
data class EventPublisherAutoConfigurationProperties(
    var infrastructureImplementation: InfrastructureImplementation = InfrastructureImplementation.KAFKA,
    var claimTimeoutMillis: Long = 300_000L,
) {
    enum class InfrastructureImplementation {
        KAFKA
    }
}
