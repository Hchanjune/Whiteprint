package com.hc.service.user.command.adapter.out.messaging

import com.hc.core.domain.model.aggregate.Aggregate
import com.hc.core.domain.event.DomainEvent
import com.hc.core.domain.event.IntegrationEvent
import com.hc.user.command.application.port.out.UserEventPublisher
import org.springframework.stereotype.Component

@Component
class UserEventPublisherImpl(
    private val domainEventPublisher: DomainEventPublisher,
    private val kafkaEventPublisher: KafkaEventPublisher
): UserEventPublisher {

    override fun publish(aggregate: Aggregate<*>) {
        val events = aggregate.pullEvents()
        events.forEach { event ->
            when (event) {
                is DomainEvent -> domainEventPublisher.publish(event)
                is IntegrationEvent -> kafkaEventPublisher.publish(event)
            }
        }
    }

}