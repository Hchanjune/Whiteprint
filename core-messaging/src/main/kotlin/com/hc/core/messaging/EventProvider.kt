package com.hc.core.messaging

interface EventProvider<T> {
    fun publishFrom(source: T)
}