package org.whiteprint.platform.adapter.event.outbox.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.JpaEventOutboxConfiguration

@AutoConfiguration
@Import(
    JpaEventOutboxConfiguration::class
)
@EnableConfigurationProperties(
    EventOutboxAutoConfigurationProperties::class,
)
class EventOutboxAutoConfiguration