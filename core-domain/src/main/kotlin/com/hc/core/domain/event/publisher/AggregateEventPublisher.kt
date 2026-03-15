package com.hc.core.domain.event.publisher

import com.hc.core.domain.model.aggregate.Aggregate

interface AggregateEventPublisher {
    fun publish(aggregate: Aggregate<*>)
}