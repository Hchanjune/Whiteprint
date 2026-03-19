package org.whiteprint.platform.infra.messaging.kafka.configuration

import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventDeserializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventSerializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaTopicResolver
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import org.whiteprint.platform.core.messaging.contract.TopicResolver

@Configuration
@EnableConfigurationProperties(KafkaConfigurationProperties::class)
class KafkaConfiguration(
    private val kafkaProperties: KafkaConfigurationProperties
) {

    @Primary
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

    @Primary
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

    @Primary
    @Bean
    fun consumerFactory(): ConsumerFactory<Long, EventEnvelope> {
        val config = mutableMapOf<String, Any>()
        val datasource = kafkaProperties.datasource
        val consumer = kafkaProperties.consumer

        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "${datasource.host}:${datasource.port}"
        config[ConsumerConfig.GROUP_ID_CONFIG] = consumer.groupId
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = consumer.autoOffsetReset
        config[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = consumer.enableAutoCommit
        config[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = consumer.isolationLevel

        return DefaultKafkaConsumerFactory(
            config,
            LongDeserializer(),
            KafkaEventDeserializer()
        )
    }

    @Primary
    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Long, EventEnvelope>,
        kafkaTemplate: KafkaTemplate<Long, EventEnvelope> // DLT 전송용 템플릿 주입
    ): ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope> {
        val factory = ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope>()
        factory.setConsumerFactory(consumerFactory)
        factory.setConcurrency(kafkaProperties.consumer.concurrency)

        val retry = kafkaProperties.retryPolicy
        val backoff = FixedBackOff(retry.backoffInterval, retry.maxAttempts)

        val errorHandler = if (retry.deadLetterTopicEnabled) {
            val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, _ ->
                TopicPartition("${record.topic()}.DLT", -1)
            }
            DefaultErrorHandler(recoverer, backoff)
        } else {
            DefaultErrorHandler(backoff)
        }

        factory.setCommonErrorHandler(errorHandler)
        return factory
    }

    @Primary
    @Bean
    fun producerFactory(): ProducerFactory<Long, EventEnvelope> {
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
            KafkaEventSerializer()
        )
    }

    @Primary
    @Bean
    fun kafkaTemplate(
        producerFactory: ProducerFactory<Long, EventEnvelope>,
    ): KafkaTemplate<Long, EventEnvelope> {
        return KafkaTemplate(producerFactory)
    }


}