package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.kernel.serializer.JsonSerializer
import com.hc.core.messaging.policy.EventEnvelope
import com.hc.core.messaging.policy.EventException
import com.hc.core.messaging.policy.EventPolicy
import org.apache.kafka.common.serialization.Serializer

class KafkaEventSerializer: Serializer<EventEnvelope<*>> {

    private val serializer = JsonSerializer.default

    override fun serialize(topic: String, data: EventEnvelope<*>): ByteArray {
        return try {
            serializer.writeValueAsBytes(data)
        } catch (exception: Exception) {
            throw EventException(
                policy = EventPolicy.SERIALIZATION_FAILED,
                attributes = mapOf(
                    "topic" to topic,
                    "partitionKey" to data.partitionKey,
                    "eventId" to data.eventId,
                    "eventName" to data.eventName
                ),
                cause = exception
            )
        }
    }

}