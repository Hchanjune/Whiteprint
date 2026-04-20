package org.whiteprint.platform.core.messaging.inbox

enum class EventInboxStatus {
    RECEIVED,
    PROCESSING,
    COMPLETED,
    FAILED,
    DEAD
}