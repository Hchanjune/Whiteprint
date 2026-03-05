package com.hc.core.event

interface EventHolder<E: Event> {
    fun record(event: E)
    fun pullEvents(): List<E>
}