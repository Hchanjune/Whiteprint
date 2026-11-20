package org.whiteprint.platform.adapter.event.inbox.configuration.purge

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.whiteprint.platform.adapter.event.inbox.configuration.EventInboxAutoConfigurationProperties
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore

/** `adapter.event.inbox.purge.enabled=true` 일 때만 정리 스케줄러를 등록한다(기본 꺼짐 — 하위호환). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "adapter.event.inbox.purge", name = ["enabled"], havingValue = "true")
class EventInboxPurgeConfiguration {

    @Bean
    fun eventInboxPurgeScheduler(
        store: EventInboxStore,
        properties: EventInboxAutoConfigurationProperties,
    ): EventInboxPurgeScheduler = EventInboxPurgeScheduler(store, properties)

}
