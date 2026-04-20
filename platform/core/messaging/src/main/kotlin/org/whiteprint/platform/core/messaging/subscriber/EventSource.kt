package org.whiteprint.platform.core.messaging.subscriber

interface EventSource {
    fun start()
    fun stop()
}