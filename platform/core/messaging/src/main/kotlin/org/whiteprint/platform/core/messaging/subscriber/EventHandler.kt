package org.whiteprint.platform.core.messaging.subscriber

import org.whiteprint.platform.core.messaging.model.Event
import kotlin.reflect.KClass

interface EventHandler<E: Event> {
    val eventType: String
    val eventClass: KClass<E>
    suspend fun handle(event: E)
}