package org.whiteprint.platform.adapter.messaging.subscriber.configuration.kafka

import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventDeserializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer

@Configuration
@ConditionalOnProperty(prefix = "adapter.messaging.subscriber", name = ["infrastructureImplementation"], havingValue = "KAFKA")
class KafkaConsumerConfiguration(
    private val kafkaProperties: KafkaConfigurationProperties
) {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean
    fun consumerFactory(
        serializer: Serializer
    ): ConsumerFactory<Long, EventEnvelope> {
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
            KafkaEventDeserializer(serializer)
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Long, EventEnvelope>,
        kafkaTemplate: KafkaTemplate<Long, EventEnvelope>
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

    @Bean("consumerProducerFactory")
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

    @Bean("subscriberKafkaTemplate")
    fun kafkaTemplate(
        @Qualifier("consumerProducerFactory")producerFactory: ProducerFactory<Long, EventEnvelope>,
    ): KafkaTemplate<Long, EventEnvelope> {
        return KafkaTemplate(producerFactory)
    }

}