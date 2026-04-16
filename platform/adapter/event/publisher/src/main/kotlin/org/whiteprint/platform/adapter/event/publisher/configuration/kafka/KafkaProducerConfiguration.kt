package org.whiteprint.platform.adapter.event.publisher.configuration.kafka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventSerializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaTopicResolver
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.whiteprint.platform.adapter.event.publisher.poller.ScheduledEventPoller
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.producer.EventPoller
import org.whiteprint.platform.core.messaging.producer.EventProducer
import org.whiteprint.platform.infra.messaging.kafka.provider.KafkaEventProducer
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer
import java.util.concurrent.TimeUnit

@Configuration
@ConditionalOnProperty(prefix = "adapter.event.publisher", name = ["infrastructure-implementation"], havingValue = "kafka", matchIfMissing = false)
class KafkaProducerConfiguration(
    private val kafkaProperties: KafkaProducerConfigurationProperties
) {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean("producerTopicResolver")
    fun topicResolver(): TopicResolver {
        val topicPartRegex = Regex("^[a-z0-9-]+$")
        val producer = kafkaProperties.topicPolicy
        val parts = listOf(producer.prefix, producer.topic, producer.version)

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
            topic = producer.topic.lowercase(),
            version = producer.version.lowercase(),
            separator = producer.separator,
        )
    }

    @Bean("producerConnectionValidator")
    fun kafkaConnectionValidator() =
        SmartInitializingSingleton {
            val props = mapOf<String, Any>(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to
                        "${kafkaProperties.datasource.host}:${kafkaProperties.datasource.port}",
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG to 3000,
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG to 3000
            )

            AdminClient.create(props).use { admin ->
                val cluster = admin.describeCluster()
                cluster.nodes().get(3, TimeUnit.SECONDS)
            }
        }

    @Bean
    fun autoCreateTopic(
        @Qualifier("producerTopicResolver") topicResolver: TopicResolver,
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

    @Bean
    fun eventProducer(
        @Qualifier("producerTopicResolver")topicResolver: TopicResolver,
        @Qualifier("producerKafkaTemplate") kafkaTemplate: KafkaTemplate<Long, EventEnvelope>,
        eventPublisher: ApplicationEventPublisher
    ): EventProducer =
        KafkaEventProducer(
            topicResolver = topicResolver,
            kafkaTemplate = kafkaTemplate,
            eventPublisher = eventPublisher,
        )

    @Bean("producerKafkaEventPoller")
    fun eventPoller(
        outboxEventStore: EventOutboxStore,
        eventEnveloper: EventEnveloper,
        producer: EventProducer,
    ): EventPoller =
        ScheduledEventPoller(
            outboxEventStore = outboxEventStore,
            eventEnveloper = eventEnveloper,
            producer = producer
        )

}