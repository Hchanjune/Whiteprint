package org.whiteprint.platform.adapter.messaging.producer.configuration.kafka

import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventSerializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaTopicResolver
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer

@Configuration
@ConditionalOnProperty(prefix = "adapter.messaging.producer", name = ["infrastructureImplementation"], havingValue = "KAFKA")
class KafkaProducerConfiguration(
    private val kafkaProperties: KafkaConfigurationProperties
) {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean
    fun topicResolver(): TopicResolver {
        val topicPartRegex = Regex("^[a-z0-9-]+$")
        val producer = kafkaProperties.topicPolicy
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
    ): NewTopic? {
        if (!kafkaProperties.topicPolicy.autoCreate) return null
        return TopicBuilder.name(topicResolver.resolve())
            .partitions(kafkaProperties.topicPolicy.defaultPartitions)
            .replicas(kafkaProperties.topicPolicy.defaultReplicationFactor)
            .configs(mapOf(
                "retention.ms" to kafkaProperties.topicPolicy.retentionMillis.toString(),
                "cleanup.policy" to kafkaProperties.topicPolicy.cleanupPolicy
            ))
            .build()
    }

    @Bean("producerProducerFactory")
    fun producerFactory(
        serializer: Serializer,
    ): ProducerFactory<Long, EventEnvelope> {
        val config = mutableMapOf<String, Any>()
        val ds = kafkaProperties.datasource
        val producer = kafkaProperties.producer

        config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "${ds.host}:${ds.port}"


        config[ProducerConfig.ACKS_CONFIG] = producer.acks
        config[ProducerConfig.BATCH_SIZE_CONFIG] = producer.batchSize
        config[ProducerConfig.LINGER_MS_CONFIG] = producer.lingerMillis
        config[ProducerConfig.COMPRESSION_TYPE_CONFIG] = producer.compressionType
        config[ProducerConfig.RETRIES_CONFIG] = producer.retries

        if (producer.acks == "all") {
            config[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
        }

        return DefaultKafkaProducerFactory(
            config,
            LongSerializer(),
            KafkaEventSerializer(serializer)
        )
    }

    @Bean("producerKafkaTemplate")
    fun kafkaTemplate(
        @Qualifier("producerProducerFactory")producerFactory: ProducerFactory<Long, EventEnvelope>,
    ): KafkaTemplate<Long, EventEnvelope> {
        return KafkaTemplate(producerFactory)
    }

}