package org.whiteprint.platform.infra.messaging.kafka.provider

import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.core.KafkaTemplate
import org.whiteprint.platform.core.messaging.model.event.EventScope
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.contract.TopicResolver
import org.whiteprint.platform.core.messaging.publisher.EventPublisher

class KafkaEventPublisher(
    private val topicResolver: TopicResolver,
    private val kafkaTemplate: KafkaTemplate<Long, EventEnvelope>,
    private val eventPublisher: ApplicationEventPublisher
): EventPublisher {

    override fun publish(envelope: EventEnvelope) {
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
            topicResolver.resolve(envelope),
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