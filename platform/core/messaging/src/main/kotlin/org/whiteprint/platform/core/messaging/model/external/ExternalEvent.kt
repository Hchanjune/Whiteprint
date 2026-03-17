package org.whiteprint.platform.core.messaging.model.external

import org.whiteprint.platform.core.messaging.model.Event

interface ExternalEvent<out T>: org.whiteprint.platform.core.messaging.model.Event<T> {
    val version: String
}