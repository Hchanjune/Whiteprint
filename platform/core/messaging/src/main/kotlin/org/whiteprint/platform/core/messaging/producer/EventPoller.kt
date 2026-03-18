package org.whiteprint.platform.core.messaging.producer

interface EventPoller {

    fun pollOnce(): Int

}