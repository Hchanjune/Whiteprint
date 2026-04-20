package org.whiteprint.platform.infra.messaging.kafka.provider

import org.whiteprint.platform.core.messaging.contract.EventSerializer
import org.whiteprint.platform.core.messaging.inbox.EventConsumer
import org.whiteprint.platform.core.messaging.subscriber.EventSource

class KafkaEventSource(
    private val consumer: EventConsumer,
    private val serializer: EventSerializer,
): EventSource {
    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }
}