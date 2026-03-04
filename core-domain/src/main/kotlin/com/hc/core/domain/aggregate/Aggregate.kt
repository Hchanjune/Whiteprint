package com.hc.core.domain.aggregate

import com.hc.core.event.DomainEvent
import java.io.Serializable

abstract class Aggregate {

    abstract val id: Serializable

    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    protected fun record(domainEvent: DomainEvent) {
        domainEvents.add(domainEvent)
    }

    fun pullEvents(): List<DomainEvent> {
        if (domainEvents.isEmpty()) return emptyList()
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }

}