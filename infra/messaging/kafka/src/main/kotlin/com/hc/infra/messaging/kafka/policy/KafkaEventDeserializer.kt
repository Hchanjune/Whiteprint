package com.hc.infra.messaging.kafka.policy

import com.hc.core.kernel.serializer.JsonSerializer
import com.hc.core.messaging.policy.EventEnvelope
import com.hc.core.messaging.policy.EventException
import com.hc.core.messaging.policy.EventPolicy
import org.apache.kafka.common.serialization.Deserializer

class KafkaEventDeserializer: Deserializer<EventEnvelope<*>> {

    private val serializer = JsonSerializer.default

    override fun deserialize(topic: String, data: ByteArray): EventEnvelope<*> {
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