package com.hc.core.event.contract

import com.hc.core.event.IntegrationEvent

interface TopicPolicy {

    fun resolve(event: IntegrationEvent)

    companion object {
        private const val PROJECT_PREFIX = ""
    }

}