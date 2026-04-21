package org.whiteprint.platform.adapter.event.subscriber.configuration

import org.springframework.context.SmartLifecycle
import org.whiteprint.platform.core.messaging.subscriber.EventSubscriber

class EventSubscriberLifecycle(
    private val subscriber: EventSubscriber,
): SmartLifecycle {

    private var running = false

    override fun start() {
        subscriber.start()
        running = true
    }

    override fun stop() {
        subscriber.stop()
        running = false
    }

    override fun isRunning(): Boolean = running
}