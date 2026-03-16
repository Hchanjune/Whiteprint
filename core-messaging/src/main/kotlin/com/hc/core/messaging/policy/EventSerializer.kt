package com.hc.core.messaging.policy

import com.hc.core.messaging.model.Event

interface EventSerializer {

    fun<E: Event> serialize(event: E): String

    fun<E: Event> deserializeRaw(raw: String, type: Class<E>): E

}