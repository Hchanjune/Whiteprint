package org.whiteprint.platform.adapter.event.outbox.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.event.outbox.configuration.jpa.JpaEventOutboxConfiguration
import org.whiteprint.platform.adapter.event.outbox.configuration.mongo.MongoEventOutboxConfiguration

@AutoConfiguration
@Import(
    JpaEventOutboxConfiguration::class,
    MongoEventOutboxConfiguration::class,
)
@EnableConfigurationProperties(
    EventOutboxAutoConfigurationProperties::class,
)
class EventOutboxAutoConfiguration