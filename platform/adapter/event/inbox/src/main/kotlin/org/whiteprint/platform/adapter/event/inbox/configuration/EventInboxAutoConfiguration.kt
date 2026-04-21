package org.whiteprint.platform.adapter.event.inbox.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.JpaEventInboxConfiguration

@AutoConfiguration
@Import(
    JpaEventInboxConfiguration::class,
)
@EnableConfigurationProperties(EventInboxAutoConfigurationProperties::class)
class EventInboxAutoConfiguration