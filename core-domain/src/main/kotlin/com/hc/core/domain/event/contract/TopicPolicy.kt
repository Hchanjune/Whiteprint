package com.hc.core.domain.event.contract

import com.hc.core.domain.event.IntegrationEvent

interface TopicPolicy {

    fun resolve(event: IntegrationEvent)

    companion object {
        private const val PROJECT_PREFIX = ""
    }

}