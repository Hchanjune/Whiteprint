package org.whiteprint.platform.adapter.event.inbox.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(EventInboxAutoConfigurationProperties::class)
class EventInboxAutoConfiguration {

}