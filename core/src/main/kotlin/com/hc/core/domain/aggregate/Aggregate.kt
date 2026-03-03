package com.hc.core.domain.aggregate

import com.hc.core.domain.entity.Persistable
import com.hc.core.event.DomainEvent

abstract class Aggregate<ID, E: Persistable<ID>> {

    abstract val id: ID
    abstract fun toPersistence(): E

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