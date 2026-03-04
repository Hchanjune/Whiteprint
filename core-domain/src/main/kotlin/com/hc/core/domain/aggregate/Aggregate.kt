package com.hc.core.domain.aggregate

import com.hc.core.domain.contract.Auditable
import com.hc.core.event.DomainEvent
import com.hc.core.event.EventHolder
import java.io.Serializable

abstract class Aggregate<ROOT: Any>:
    Auditable,
    EventHolder<DomainEvent> {

    abstract val id: Serializable
    abstract val root: ROOT
    val aggregateType: String = root::class.simpleName?: "UNKNOWN"

    private val events: MutableList<DomainEvent> = mutableListOf()

    override fun record(event: DomainEvent) {
        events.add(event)
    }

    override fun pullEvents(): List<DomainEvent> {
        if (events.isEmpty()) return emptyList()
        val readOnly = events.toList()
        events.clear()
        return readOnly
    }

}