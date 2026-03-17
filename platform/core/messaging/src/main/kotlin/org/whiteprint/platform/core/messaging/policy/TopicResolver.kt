package org.whiteprint.platform.core.messaging.policy

interface TopicResolver {
    fun resolve(): String
}