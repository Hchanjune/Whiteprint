package com.hc.user.command.adapter.out.messaging

import com.hc.core.event.Event
import com.hc.core.event.publisher.EventPublisher
import org.springframework.stereotype.Component

@Component
class KafkaEventPublisher: EventPublisher {
    override fun <T : Event> publish(event: T) {
        TODO("Not yet implemented")
    }
}