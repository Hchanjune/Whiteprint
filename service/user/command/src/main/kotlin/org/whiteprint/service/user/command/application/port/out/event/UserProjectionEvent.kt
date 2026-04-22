package org.whiteprint.service.user.command.application.port.out.event

import org.whiteprint.platform.core.messaging.model.event.external.IntegrationEvent

data class UserProjectionEvent(
    override val eventType: String = "",
    override val schemaVersion: String = "",
    val userId: String
): IntegrationEvent