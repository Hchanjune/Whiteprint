package com.hc.user.command.adapter.out.messaging

import com.hc.core.event.Event
import com.hc.core.event.publisher.EventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
): EventPublisher {

    override fun <T : Event> publish(event: T) {
        applicationEventPublisher.publishEvent(event)
    }

}