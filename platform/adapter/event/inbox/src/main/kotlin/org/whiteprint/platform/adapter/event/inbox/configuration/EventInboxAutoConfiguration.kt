package org.whiteprint.platform.adapter.event.inbox.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.event.inbox.configuration.jpa.JpaEventInboxConfiguration
import org.whiteprint.platform.adapter.event.inbox.configuration.mongo.MongoEventInboxConfiguration

@AutoConfiguration
@Import(
    JpaEventInboxConfiguration::class,
    MongoEventInboxConfiguration::class,
)
@EnableConfigurationProperties(EventInboxAutoConfigurationProperties::class)
class EventInboxAutoConfiguration