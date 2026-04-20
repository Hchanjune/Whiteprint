package org.whiteprint.platform.core.messaging.publisher

interface EventPoller {

    fun pollOnce(): Int

}