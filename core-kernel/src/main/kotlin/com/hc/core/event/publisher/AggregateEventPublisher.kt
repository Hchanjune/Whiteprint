package com.hc.core.event.publisher

import com.hc.core.domain.aggregate.Aggregate

interface AggregateEventPublisher {
    fun publish(aggregate: Aggregate<*>)
}