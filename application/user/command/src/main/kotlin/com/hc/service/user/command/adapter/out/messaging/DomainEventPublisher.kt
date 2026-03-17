package com.hc.service.user.command.adapter.out.messaging

import com.hc.core.platform.event.Event
import com.hc.core.platform.event.publisher.EventPublisher
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