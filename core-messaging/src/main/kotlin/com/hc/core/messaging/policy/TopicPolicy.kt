package com.hc.core.messaging.policy

import com.hc.core.messaging.model.external.IntegrationEvent

interface TopicPolicy {

    fun resolve(event: IntegrationEvent)

    companion object {
        private const val PROJECT_PREFIX = ""
    }

}