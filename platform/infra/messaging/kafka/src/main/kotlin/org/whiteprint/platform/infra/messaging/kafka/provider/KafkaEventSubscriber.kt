package org.whiteprint.platform.infra.messaging.kafka.provider

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

    private var container: ConcurrentMessageListenerContainer<Long, EventEnvelope>? = null

    override fun start() {
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