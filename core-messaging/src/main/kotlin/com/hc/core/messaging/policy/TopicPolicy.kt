package com.hc.core.messaging.policy

interface TopicPolicy {

    fun resolve(event: IntegrationEvent)

    companion object {
        private const val PROJECT_PREFIX = ""
    }

}