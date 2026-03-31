package org.whiteprint.platform.adapter.event.outbox.configuration.jpa

import org.springframework.context.annotation.Configuration

@Configuration
class JpaEventOutboxConfiguration(
    private val properties: JpaEventOutboxConfigurationProperties
) {

}