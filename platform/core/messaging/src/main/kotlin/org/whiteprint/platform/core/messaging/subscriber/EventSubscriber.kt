package org.whiteprint.platform.core.messaging.subscriber

interface EventSubscriber {
    fun start()
    fun stop()
}