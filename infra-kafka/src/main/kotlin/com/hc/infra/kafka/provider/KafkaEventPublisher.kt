package com.hc.infra.kafka.provider

import com.hc.core.messaging.model.Event
import com.hc.core.messaging.model.external.ExternalEvent
import com.hc.core.messaging.model.internal.InternalEvent
import com.hc.core.messaging.policy.EventEnvelope
import com.hc.core.messaging.policy.EventEnveloper
import com.hc.core.messaging.policy.TopicResolver
import com.hc.core.messaging.provider.EventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.core.KafkaTemplate

@Deprecated("Planning to use outbox polling policy")
class KafkaEventPublisher(
    private val topicResolver: TopicResolver,
    private val enveloper: EventEnveloper,
    private val kafkaTemplate: KafkaTemplate<Long, EventEnvelope<*>>,
    private val eventPublisher: ApplicationEventPublisher
): EventPublisher {

    override fun <E : Event> publish(event: E) {
        val envelope = enveloper.envelope(event)
        when (event) {
            is InternalEvent -> {
                sendInternal(envelope)
            }
            is ExternalEvent -> {
                sendExternal(envelope)
            }
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