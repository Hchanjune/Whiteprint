package org.whiteprint.platform.infra.messaging.kafka.policy

import org.whiteprint.platform.core.kernel.serializer.JsonSerializer
import org.whiteprint.platform.core.messaging.policy.EventEnvelope
import org.whiteprint.platform.core.messaging.policy.EventException
import org.whiteprint.platform.core.messaging.policy.EventPolicy
import org.apache.kafka.common.serialization.Deserializer

class KafkaEventDeserializer: Deserializer<org.whiteprint.platform.core.messaging.policy.EventEnvelope<*>> {

    private val serializer = JsonSerializer.default

    override fun deserialize(topic: String, data: ByteArray): org.whiteprint.platform.core.messaging.policy.EventEnvelope<*> {
        return try {
            serializer.readValue(data, EventEnvelope::class.java)
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