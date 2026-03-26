package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.messaging.model.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import org.apache.kafka.common.serialization.Deserializer
import org.whiteprint.platform.core.kernel.serializer.Serializer

class KafkaEventDeserializer(
    private val serializer: Serializer,
): Deserializer<EventEnvelope> {

    override fun deserialize(topic: String, data: ByteArray): EventEnvelope {
        return try {
            serializer.deserializeFromBytes(data, EventEnvelope::class.java)
        } catch (exception: Exception) {
            throw EventException(
                policy = EventPolicy.DESERIALIZATION_FAILED,
                attributes = mapOf(
                    "topic" to topic,
                    "dataSize" to data.size
                ),
                cause = exception
            )
        }
    }

}