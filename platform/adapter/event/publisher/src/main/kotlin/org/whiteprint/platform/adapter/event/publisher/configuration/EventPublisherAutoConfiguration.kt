package org.whiteprint.platform.adapter.event.publisher.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.event.publisher.configuration.kafka.KafkaProducerConfiguration
import org.whiteprint.platform.adapter.event.publisher.configuration.kafka.KafkaProducerConfigurationProperties

@AutoConfiguration
@Import(
    KafkaProducerConfiguration::class
)
@EnableConfigurationProperties(
    EventPublisherAutoConfigurationProperties::class,
    KafkaProducerConfigurationProperties::class,
)
class EventPublisherAutoConfiguration