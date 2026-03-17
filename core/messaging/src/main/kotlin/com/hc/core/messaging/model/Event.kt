package com.hc.core.messaging.model

interface Event<out T> {
    val name: String
    val schemaVersion: String
    val key: Long
    val payload: T
}