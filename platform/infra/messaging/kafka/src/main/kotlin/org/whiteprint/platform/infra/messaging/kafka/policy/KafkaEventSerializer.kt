package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.kernel.serializer.DefaultSerializer
import org.apache.kafka.common.serialization.Serializer
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy

class KafkaEventSerializer: Serializer<EventEnvelope> {

    private val serializer = DefaultSerializer.jsonMapper

    override fun serialize(topic: String, data: EventEnvelope): ByteArray {
        return try {
            serializer.writeValueAsBytes(data)
        } catch (exception: Exception) {
            throw EventException(
                policy = EventPolicy.SERIALIZATION_FAILED,
                attributes = mapOf(
                    "topic" to topic,
                    "partitionKey" to data.partitionKey,
                    "eventId" to data.eventId,
                    "eventType" to data.eventType
                ),
                cause = exception
            )
        }
    }

}