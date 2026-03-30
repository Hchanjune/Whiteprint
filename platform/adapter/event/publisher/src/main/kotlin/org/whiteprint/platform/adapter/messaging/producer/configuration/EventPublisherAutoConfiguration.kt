package org.whiteprint.platform.adapter.messaging.producer.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.messaging.producer.configuration.kafka.KafkaProducerConfiguration
import org.whiteprint.platform.adapter.messaging.producer.configuration.kafka.KafkaProducerConfigurationProperties

@AutoConfiguration
@Import(
    KafkaProducerConfiguration::class
)
@EnableConfigurationProperties(
    EventPublisherAutoConfigurationProperties::class,
    KafkaProducerConfigurationProperties::class,
)
class EventPublisherAutoConfiguration {
}