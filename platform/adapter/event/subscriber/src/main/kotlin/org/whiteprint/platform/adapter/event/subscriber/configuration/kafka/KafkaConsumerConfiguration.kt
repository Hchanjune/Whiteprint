package org.whiteprint.platform.adapter.event.subscriber.configuration.kafka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventDeserializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.beans.factory.SmartInitializingSingleton
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
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.subscriber.EventSubscriber
import org.whiteprint.platform.infra.messaging.kafka.provider.KafkaEventSubscriber
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer
import java.util.concurrent.TimeUnit
import kotlin.Any
import kotlin.String
import kotlin.use

@Configuration
@ConditionalOnProperty(prefix = "adapter.event.subscriber", name = ["infrastructure-implementation"], havingValue = "kafka", matchIfMissing = true)
class KafkaConsumerConfiguration(
    private val kafkaProperties: KafkaConsumerConfigurationProperties
) {

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    @Bean("consumerConnectionValidator")
    @ConditionalOnProperty(
        prefix = "adapter.event.subscriber.kafka.connection-validation",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false,
    )
    fun kafkaConnectionValidator(kafkaProperties: KafkaConsumerConfigurationProperties) =
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

        // 구독자는 토픽을 만들지 않는다(클라이언트 기본값은 true).
        // 토픽 스펙(파티션 수·retention)의 주인은 발행 서비스의 topic-policy 다.
        // 구독자가 먼저 떠서 브로커 기본값(보통 1파티션)으로 토픽을 만들어버리면,
        // 나중에 발행 서비스의 createTopics 는 TopicExistsException 으로 스킵되어
        // 선언한 스펙이 영영 적용되지 않는다 — 기동 순서에 따라 결과가 갈리게 된다.
        // 토픽이 아직 없으면 UNKNOWN_TOPIC 경고를 내며 대기하다 생성되는 즉시 붙는다.
        config[ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG] = false

        return DefaultKafkaConsumerFactory(
            config,
            LongDeserializer(),
            KafkaEventDeserializer(serializer)
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Long, EventEnvelope>,
        @Qualifier("deadLetterKafkaTemplate")kafkaTemplate: KafkaTemplate<Long, EventEnvelope>
    ): ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope> {
        val factory = ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope>()
        factory.setConsumerFactory(consumerFactory)
        factory.setConcurrency(kafkaProperties.consumer.concurrency)

        val retry = kafkaProperties.errorHandling.retry
        val deadLetter = kafkaProperties.errorHandling.deadLetter

        val backoff = FixedBackOff(retry.backoffInterval, retry.maxAttempts)

        val errorHandler = if (deadLetter.enabled) {
            val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, _ ->
                TopicPartition("${record.topic()}${deadLetter.topicSuffix}", -1)
            }
            DefaultErrorHandler(recoverer, backoff)
        } else {
            DefaultErrorHandler(backoff)
        }

        factory.setCommonErrorHandler(errorHandler)
        return factory
    }

    @Bean("deadLetterProducerFactory")
    fun deadLetterProducerFactory(
        serializer: Serializer,
    ): ProducerFactory<Long, EventEnvelope> {
        val config = mutableMapOf<String, Any>()
        val ds = kafkaProperties.datasource
        val producer = kafkaProperties.errorHandling.deadLetter.producer

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

    @Bean("deadLetterKafkaTemplate")
    fun deadLetterKafkaTemplate(
        @Qualifier("deadLetterProducerFactory")producerFactory: ProducerFactory<Long, EventEnvelope>,
    ): KafkaTemplate<Long, EventEnvelope> =
        KafkaTemplate(producerFactory)

    @Bean
    fun kafkaEventSubscriber(
        consumer: EventConsumer,
        containerFactory: ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope>,
    ): EventSubscriber {
        val prefix = kafkaProperties.subscriptionPolicy.prefix
        val version = kafkaProperties.subscriptionPolicy.version
        val separator = kafkaProperties.subscriptionPolicy.separator
        val subscribingTopics = kafkaProperties.subscriptionPolicy.eventTypes.map {
            listOf(prefix, it, version).joinToString(separator)
        }.toSet()
        return KafkaEventSubscriber(
            consumer = consumer,
            subscribingTopics = subscribingTopics,
            containerFactory = containerFactory,
        )
    }

}