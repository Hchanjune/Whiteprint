package org.whiteprint.service.user.command.domain.event

import org.whiteprint.platform.core.messaging.model.event.internal.DomainEvent

data class UserDeletedEvent(
    override val eventType: String = "",
    override val schemaVersion: String = "",
    val userId: String,
): DomainEvent