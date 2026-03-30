package org.whiteprint.platform.adapter.messaging.subscriber.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.messaging.subscriber.configuration.kafka.KafkaConsumerConfigurationProperties
import org.whiteprint.platform.adapter.messaging.subscriber.configuration.kafka.KafkaConsumerConfiguration

@AutoConfiguration
@Import(
    KafkaConsumerConfiguration::class,
)
@EnableConfigurationProperties(
    EventSubscriberAutoConfigurationProperties::class,
    KafkaConsumerConfigurationProperties::class,
)
class EventSubscriberAutoConfiguration {
}