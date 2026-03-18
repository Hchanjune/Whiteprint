package org.whiteprint.platform.core.messaging.model

interface Event {
    val eventType: String
    val schemaVersion: String
}