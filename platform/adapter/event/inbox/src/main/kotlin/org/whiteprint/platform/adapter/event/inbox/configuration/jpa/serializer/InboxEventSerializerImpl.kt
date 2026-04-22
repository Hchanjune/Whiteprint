package org.whiteprint.platform.adapter.event.inbox.configuration.jpa.serializer

import org.whiteprint.platform.core.kernel.serializer.Serializer
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer
import org.whiteprint.platform.core.messaging.model.Event

class InboxEventSerializerImpl(
    private val serializer: Serializer
): InboxEventSerializer {
    override fun toByteArray(event: Event): ByteArray {
        return serializer.serializeToBytes(event)
    }

    override fun toJson(event: Event): String {
        return serializer.serializeToJson(event)
    }

    override fun payloadToJson(payload: ByteArray): String {
        return String(payload, Charsets.UTF_8)
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