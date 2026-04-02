package org.whiteprint.platform.adapter.event.outbox.configuration.jpa.serializer

import org.whiteprint.platform.core.messaging.contract.EventSerializer
import org.whiteprint.platform.core.messaging.model.Event

class OutboxEventSerializer: EventSerializer {
    override fun toByteArray(event: Event): ByteArray {
        TODO("Not yet implemented")
    }

    override fun toJson(event: Event): String {
        TODO("Not yet implemented")
    }

    override fun metadataToJson(metadata: Map<String, String>): String {
        TODO("Not yet implemented")
    }

    override fun <T : Event> deserialize(
        bytes: ByteArray,
        type: Class<T>
    ): T {
        TODO("Not yet implemented")
    }
}