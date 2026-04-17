package org.whiteprint.platform.core.messaging.outbox

enum class EventOutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED,
}