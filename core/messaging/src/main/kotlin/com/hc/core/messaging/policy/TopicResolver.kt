package com.hc.core.messaging.policy

interface TopicResolver {
    fun resolve(): String
}