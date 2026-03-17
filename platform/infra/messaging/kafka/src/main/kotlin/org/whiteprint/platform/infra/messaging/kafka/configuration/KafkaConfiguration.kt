package org.whiteprint.platform.infra.messaging.kafka.configuration

import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventDeserializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventSerializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaTopicResolver
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import org.whiteprint.platform.core.messaging.policy.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import org.whiteprint.platform.core.messaging.policy.TopicResolver

@Configuration
@EnableConfigurationProperties(KafkaConfigurationProperties::class)
class KafkaConfiguration(
    private val kafkaProperties: KafkaConfigurationProperties
) {

    @Bean
    fun topicResolver(): TopicResolver {
        val topicPartRegex = Regex("^[a-z0-9-]+$")
        val producer = kafkaProperties.producer
        val parts = listOf(producer.prefix, producer.host, producer.topic, producer.version)

        if (producer.topic.isBlank() || producer.topic == "topic") {
            throw EventException(EventPolicy.TOPIC_NOT_CONFIGURED)
        }

        parts.filter { it.isNotBlank() }.forEach { part ->
            if (!topicPartRegex.matches(part)) {
                throw EventException(EventPolicy.INVALID_TOPIC_FORMAT)
            }
        }

        return KafkaTopicResolver(
            prefix = producer.prefix.lowercase(),
            host = producer.host.lowercase(),
            topic = producer.topic.lowercase(),
            version = producer.version.lowercase()
        )
    }

    @Bean
    fun autoCreateTopic(
        topicResolver: TopicResolver,
    ): NewTopic {
        return TopicBuilder.name(topicResolver.resolve())
            .partitions(kafkaProperties.producer.partitions)
            .replicas(kafkaProperties.producer.replicationFactor)
            .build()
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<Long, EventEnvelope<*>> {
        val config = mutableMapOf<String, Any>()
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "${kafkaProperties.host}:${kafkaProperties.port}"
        val keyDeserializer = LongDeserializer()
        val valueDeserializer = KafkaEventDeserializer()
        return DefaultKafkaConsumerFactory(config, keyDeserializer, valueDeserializer)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Long, EventEnvelope<*>>
    ): ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope<*>> {
        val factory = ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope<*>>()
        factory.setConsumerFactory(consumerFactory)
        factory.setConcurrency(kafkaProperties.listener.concurrency)
        val backoff = FixedBackOff(kafkaProperties.retry.backoffInterval, kafkaProperties.retry.maxAttempts)
        factory.setCommonErrorHandler(DefaultErrorHandler(backoff))
        return factory
    }

    @Bean
    fun producerFactory(): ProducerFactory<Long, EventEnvelope<*>> {
        val config = mutableMapOf<String, Any>()
        config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "${kafkaProperties.host}:${kafkaProperties.port}"
        config[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
        config[ProducerConfig.ACKS_CONFIG] = "all"
        val keySerializer = LongSerializer()
        val valueSerializer = KafkaEventSerializer()
        return DefaultKafkaProducerFactory(config, keySerializer, valueSerializer)
    }

    @Bean
    fun kafkaTemplate(
        producerFactory: ProducerFactory<Long, EventEnvelope<*>>,
    ): KafkaTemplate<Long, EventEnvelope<*>> {
        return KafkaTemplate(producerFactory)
    }


}