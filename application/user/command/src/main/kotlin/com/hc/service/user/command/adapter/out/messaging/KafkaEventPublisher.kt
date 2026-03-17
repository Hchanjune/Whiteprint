package com.hc.service.user.command.adapter.out.messaging

import com.hc.core.platform.event.Event
import com.hc.core.platform.event.publisher.EventPublisher
import org.springframework.stereotype.Component

@Component
class KafkaEventPublisher: EventPublisher {
    override fun <T : Event> publish(event: T) {
        TODO("Not yet implemented")
    }
}