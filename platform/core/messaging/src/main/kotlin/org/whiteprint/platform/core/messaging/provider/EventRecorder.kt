package org.whiteprint.platform.core.messaging.provider

import org.whiteprint.platform.core.messaging.model.Event

interface EventRecorder {
    val events: List<org.whiteprint.platform.core.messaging.model.Event<Any>>
    fun record(event: org.whiteprint.platform.core.messaging.model.Event<Any>)
    fun pullEvents(): List<org.whiteprint.platform.core.messaging.model.Event<Any>>
}