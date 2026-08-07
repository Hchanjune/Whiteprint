package org.whiteprint.platform.infra.messaging.kafka.provider

import org.slf4j.LoggerFactory
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.subscriber.EventSubscriber

class KafkaEventSubscriber(
    private val consumer: EventConsumer,
    private val subscribingTopics: Set<String>,
    private val containerFactory: ConcurrentKafkaListenerContainerFactory<Long, EventEnvelope>,
): EventSubscriber {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var container: ConcurrentMessageListenerContainer<Long, EventEnvelope>? = null

    override fun start() {
        // 구독 토픽이 하나도 없으면 컨테이너를 아예 만들지 않는다.
        // spring-kafka 는 토픽 0개짜리 컨테이너 생성을 거부하므로(ConsumerProperties 의
        // "An array of topics must be provided" assert), 여기서 막지 않으면 아직 수신할
        // 이벤트가 없는 서비스가 기동 자체를 하지 못한다.
        if (subscribingTopics.isEmpty()) {
            logger.warn(
                "No subscribing topics — Kafka listener container not started. " +
                    "구독할 이벤트 타입이 없어 Kafka 리스너를 기동하지 않습니다. " +
                    "수신이 필요하다면 adapter.event.subscriber.kafka.subscription-policy.event-types 를 채우세요."
            )
            return
        }

        val container = containerFactory.createContainer(*subscribingTopics.toTypedArray())
        container.setupMessageListener(
            MessageListener<Long, EventEnvelope> { record ->
                consumer.consume(record.value())
            }
        )
        container.start()
        this.container = container
    }

    override fun stop() {
        container?.stop()
        container = null
    }

}