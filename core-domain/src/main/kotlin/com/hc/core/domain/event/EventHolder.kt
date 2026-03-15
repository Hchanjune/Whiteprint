package com.hc.core.domain.event

interface EventHolder<E: Event> {
    fun record(event: E)
    fun pullEvents(): List<E>
}