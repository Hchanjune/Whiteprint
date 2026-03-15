package com.hc.core.domain.model.aggregate

import com.hc.core.domain.model.contract.Auditable
import com.hc.core.domain.model.contract.Identifiable
import com.hc.core.domain.model.contract.LifeCycle
import com.hc.core.domain.model.contract.Recordable
import com.hc.core.domain.event.Event
import com.hc.core.domain.event.EventHolder
import java.io.Serializable

abstract class Aggregate<ROOT: Any>:
    Identifiable<Serializable>,
    Auditable,
    EventHolder<Event>,
    Recordable<Event>,
    LifeCycle {
    abstract val schemaVersion: String
    abstract override val id: Serializable
    abstract val root: ROOT
    val aggregateType: String by lazy { root::class.simpleName ?: "UNKNOWN" }

    private val _events: MutableList<Event> = mutableListOf()
    override val events: List<Event> get() = _events.toList()

    override fun record(event: Event) {
        _events.add(event)
    }

    override fun pullEvents(): List<Event> {
        return _events.toList().also { _events.clear() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aggregate<*>) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

}