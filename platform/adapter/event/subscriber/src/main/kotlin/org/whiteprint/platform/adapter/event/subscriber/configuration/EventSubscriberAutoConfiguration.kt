package org.whiteprint.platform.adapter.event.subscriber.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.event.subscriber.configuration.kafka.KafkaConsumerConfigurationProperties
import org.whiteprint.platform.adapter.event.subscriber.configuration.kafka.KafkaConsumerConfiguration
import org.whiteprint.platform.core.messaging.subscriber.EventSubscriber

@AutoConfiguration
@Import(
    KafkaConsumerConfiguration::class,
)
@EnableConfigurationProperties(
    EventSubscriberAutoConfigurationProperties::class,
    KafkaConsumerConfigurationProperties::class,
)
class EventSubscriberAutoConfiguration {

    @Bean
    fun eventSubscriberLifeCycle(
        subscriber: EventSubscriber,
    ): EventSubscriberLifecycle =
        EventSubscriberLifecycle(subscriber)

}