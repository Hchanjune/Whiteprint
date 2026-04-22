package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.serializer

import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.model.Event
import org.whiteprint.platform.core.messaging.outbox.OutboxEventSerializer

class OutboxEventSerializerImpl(
    private val serializer: Serializer
): OutboxEventSerializer {
    override fun toByteArray(event: Event): ByteArray {
        return serializer.serializeToBytes(event)
    }

    override fun toJson(event: Event): String {
        return serializer.serializeToJson(event)
    }

    override fun payloadToJson(payload: ByteArray): String {
        return serializer.serializeToJson(payload)
    }

    override fun metadataToJson(metadata: Map<String, String>): String {
        return serializer.serializeToJson(metadata)
    }

    override fun <T : Event> deserialize(
        bytes: ByteArray,
        type: Class<T>
    ): T {
        return serializer.deserializeFromBytes(bytes, type)
    }
}