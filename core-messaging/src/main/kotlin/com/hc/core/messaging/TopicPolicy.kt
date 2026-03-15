package com.hc.core.messaging

interface TopicPolicy {

    fun resolve(event: IntegrationEvent)

    companion object {
        private const val PROJECT_PREFIX = ""
    }

}