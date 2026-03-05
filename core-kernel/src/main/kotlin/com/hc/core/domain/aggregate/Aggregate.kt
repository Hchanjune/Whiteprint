package com.hc.core.domain.aggregate

import com.hc.core.domain.contract.Auditable
import com.hc.core.domain.contract.Identifiable
import com.hc.core.domain.contract.LifeCycle
import com.hc.core.domain.contract.Recordable
import com.hc.core.event.DomainEvent
import com.hc.core.event.EventHolder
import java.io.Serializable

abstract class Aggregate<ROOT: Any>:
    Identifiable<Serializable>,
    Auditable,
    EventHolder<DomainEvent>,
    Recordable<DomainEvent>,
    LifeCycle {

    abstract override val id: Serializable
    abstract val root: ROOT
    val aggregateType: String by lazy { root::class.simpleName ?: "UNKNOWN" }

    private val _events: MutableList<DomainEvent> = mutableListOf()
    override val events: List<DomainEvent> get() = _events.toList()

    override fun record(event: DomainEvent) {
        _events.add(event)
    }

    override fun pullEvents(): List<DomainEvent> {
        return _events.toList().also { _events.clear() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aggregate<*>) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

}