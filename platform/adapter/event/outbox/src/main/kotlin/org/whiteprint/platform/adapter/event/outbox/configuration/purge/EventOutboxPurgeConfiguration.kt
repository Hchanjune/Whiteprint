package org.whiteprint.platform.adapter.event.outbox.configuration.purge

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.whiteprint.platform.adapter.event.outbox.configuration.EventOutboxAutoConfigurationProperties
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore

/** `adapter.event.outbox.purge.enabled=true` 일 때만 정리 스케줄러를 등록한다(기본 꺼짐 — 하위호환). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "adapter.event.outbox.purge", name = ["enabled"], havingValue = "true")
class EventOutboxPurgeConfiguration {

    @Bean
    fun eventOutboxPurgeScheduler(
        store: EventOutboxStore,
        properties: EventOutboxAutoConfigurationProperties,
    ): EventOutboxPurgeScheduler = EventOutboxPurgeScheduler(store, properties)

}
