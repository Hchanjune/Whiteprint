package com.hc.core.event

interface EventHolder<E> {
    fun record(event: E)
    fun pullEvents(): List<E>
}