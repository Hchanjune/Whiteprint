package org.whiteprint.platform.core.messaging.contract

interface TopicResolver {
    fun resolve(): String
}