package org.whiteprint.platform.adapter.event.publisher.configuration.kafka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaEventSerializer
import org.whiteprint.platform.infra.messaging.kafka.policy.KafkaTopicResolver
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.LongSerializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.whiteprint.platform.adapter.event.publisher.configuration.EventPublisherAutoConfigurationProperties
import org.whiteprint.platform.adapter.event.publisher.poller.ScheduledEventPoller
import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.contract.EventEnveloper
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.outbox.EventOutboxStore
import org.whiteprint.platform.core.messaging.publisher.EventPoller
import org.whiteprint.platform.core.messaging.publisher.EventPublisher
import org.whiteprint.platform.infra.messaging.kafka.provider.KafkaEventPublisher
import org.whiteprint.platform.infra.serializer.jackson.JacksonSerializer
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

@Configuration
@ConditionalOnProperty(prefix = "adapter.event.publisher", name = ["infrastructure-implementation"], havingValue = "kafka", matchIfMissing = false)
class KafkaProducerConfiguration(
    private val kafkaProperties: KafkaProducerConfigurationProperties,
    private val publisherProperties: EventPublisherAutoConfigurationProperties,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnMissingBean(Serializer::class)
    fun serializer(): Serializer = JacksonSerializer()

    /**
     * 선언된 토픽을 기동 시점에 생성한다.
     *
     * Spring 의 `KafkaAdmin` 에 맡기지 않고 AdminClient 를 직접 쓰는 이유:
     * `KafkaAdmin` 은 `spring.kafka.bootstrap-servers` 를 보는데, 이 플랫폼은 브로커 주소를
     * `adapter.event.publisher.kafka.datasource` 로 받는다. 두 곳을 동기화시키느니 여기서 직접 만든다.
     *
     * `topics` 가 비어 있거나 `auto-create: false` 면 아무것도 하지 않는다 —
     * 아직 발행할 이벤트가 없는 서비스도 그대로 기동된다.
     * 브로커에 붙지 못해도 기동을 막지 않는다(접속 확인은 `connection-validation` 의 몫).
     */
    @Bean("producerTopicInitializer")
    fun topicInitializer(): SmartInitializingSingleton = SmartInitializingSingleton {
        val policy = kafkaProperties.topicPolicy
        if (!policy.autoCreate || policy.topics.isEmpty()) return@SmartInitializingSingleton

        val newTopics = policy.topics.map { (eventType, spec) ->
            val topicName = listOf(
                policy.prefix,
                eventType,
                policy.version
            ).joinToString(policy.separator)

            NewTopic(topicName, spec.partitions, spec.replicationFactor.toShort())
                .configs(
                    mapOf(
                        "retention.ms" to spec.retentionMillis.toString(),
                        "cleanup.policy" to spec.cleanupPolicy
                    )
                )
        }

        val props = mapOf<String, Any>(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to
                    "${kafkaProperties.datasource.host}:${kafkaProperties.datasource.port}",
            AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG to 3000,
            AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG to 3000
        )

        runCatching {
            AdminClient.create(props).use { admin ->
                admin.createTopics(newTopics).values().forEach { (topicName, future) ->
                    try {
                        future.get(5, TimeUnit.SECONDS)
                        logger.info("Kafka topic created — 토픽 생성됨: {}", topicName)
                    } catch (exception: Exception) {
                        val cause = (exception as? ExecutionException)?.cause ?: exception
                        if (cause is TopicExistsException) return@forEach
                        logger.warn("Kafka topic creation failed — 토픽 생성 실패: {}", topicName, cause)
                    }
                }
            }
        }.onFailure {
            logger.warn(
                "Skipping Kafka topic auto-creation, broker unreachable — " +
                    "브로커에 접속할 수 없어 토픽 자동 생성을 건너뜁니다.",
                it,
            )
        }
    }

    @Bean("producerTopicResolver")
    fun topicResolver(): TopicResolver {
        val topicPartRegex = Regex("^[a-z0-9-]+$")
        val producer = kafkaProperties.topicPolicy
        val parts = listOf(producer.prefix, producer.version)

        parts.filter { it.isNotBlank() }.forEach { part ->
            if (!topicPartRegex.matches(part)) {
                throw EventException(EventPolicy.INVALID_TOPIC_FORMAT)
            }
        }

        return KafkaTopicResolver(
            allowedTopics = kafkaProperties.topicPolicy.topics.keys.toSet(),
            prefix = producer.prefix.lowercase(),
            version = producer.version.lowercase(),
            separator = producer.separator,
        )
    }

    @Bean("producerConnectionValidator")
    @ConditionalOnProperty(
        prefix = "adapter.event.publisher.kafka.connection-validation",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false,
    )
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
    fun eventPublisher(
        @Qualifier("producerTopicResolver")topicResolver: TopicResolver,
        @Qualifier("producerKafkaTemplate") kafkaTemplate: KafkaTemplate<Long, EventEnvelope>,
        eventPublisher: ApplicationEventPublisher
    ): EventPublisher =
        KafkaEventPublisher(
            topicResolver = topicResolver,
            kafkaTemplate = kafkaTemplate,
            eventPublisher = eventPublisher,
        )

    @Bean("producerKafkaEventPoller")
    fun eventPoller(
        outboxEventStore: EventOutboxStore,
        eventEnveloper: EventEnveloper,
        producer: EventPublisher,
    ): EventPoller =
        ScheduledEventPoller(
            outboxEventStore = outboxEventStore,
            eventEnveloper = eventEnveloper,
            producer = producer,
            claimTimeoutMillis = publisherProperties.claimTimeoutMillis,
        )

}