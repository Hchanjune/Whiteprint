package com.hc.infra.kafka.core.provider

import com.hc.core.messaging.model.EventType
import com.hc.core.messaging.policy.EventEnvelope
import com.hc.core.messaging.policy.TopicResolver
import com.hc.core.messaging.provider.EventProducer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.core.KafkaTemplate

class KafkaEventProducer(
    private val topicResolver: TopicResolver,
    private val kafkaTemplate: KafkaTemplate<Long, EventEnvelope<*>>,
    private val eventPublisher: ApplicationEventPublisher
): EventProducer {

    override fun produce(envelope: EventEnvelope<*>) {
        when (envelope.eventType) {
            EventType.INTERNAL -> {
                sendInternal(envelope)
            }
            EventType.EXTERNAL -> {
                sendExternal(envelope)
            }
            else -> {}
        }
    }

    private fun sendExternal(envelope: EventEnvelope<*>) {
        kafkaTemplate.send(
            topicResolver.resolve(),
            envelope.partitionKey,
            envelope
        ).whenComplete { result, exception ->
            if (exception != null) {

            }
        }
    }

    private fun sendInternal(envelope: EventEnvelope<*>) {
        eventPublisher.publishEvent(envelope)
    }



}