package org.whiteprint.platform.core.messaging.model.external

import org.whiteprint.platform.core.messaging.model.Event

interface ExternalEvent<out T>: Event<T> {
    val version: String
}