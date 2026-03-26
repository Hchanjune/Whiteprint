package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.apache.kafka.common.serialization.Serializer as KafkaSerializer
import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy

class KafkaEventSerializer(
    private val serializer: Serializer
): KafkaSerializer<EventEnvelope> {

    override fun serialize(topic: String, data: EventEnvelope): ByteArray {
        return try {
            serializer.serializeToBytes(data)
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