package org.whiteprint.platform.core.messaging.contract

import org.whiteprint.platform.core.messaging.model.EventEnvelope

interface TopicResolver {
    fun resolve(envelope: EventEnvelope): String
}