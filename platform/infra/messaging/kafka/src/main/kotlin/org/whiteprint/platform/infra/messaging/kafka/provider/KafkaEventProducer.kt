package org.whiteprint.platform.infra.messaging.kafka.provider

import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.core.KafkaTemplate
import org.whiteprint.platform.core.messaging.model.event.EventScope
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.producer.EventProducer

class KafkaEventProducer(
    private val topicResolver: TopicResolver,
    private val kafkaTemplate: KafkaTemplate<Long, EventEnvelope>,
    private val eventPublisher: ApplicationEventPublisher
): EventProducer {

    override fun produce(envelope: EventEnvelope) {
        when (envelope.eventScope) {
            EventScope.INTERNAL -> {
                sendInternal(envelope)
            }
            EventScope.EXTERNAL -> {
                sendExternal(envelope)
            }
            else -> {}
        }
    }

    private fun sendExternal(envelope: EventEnvelope) {
        kafkaTemplate.send(
            topicResolver.resolve(),
            envelope.partitionKey,
            envelope
        ).whenComplete { result, exception ->
            if (exception != null) {

            }
        }
    }

    private fun sendInternal(envelope: EventEnvelope) {
        eventPublisher.publishEvent(envelope)
    }



}