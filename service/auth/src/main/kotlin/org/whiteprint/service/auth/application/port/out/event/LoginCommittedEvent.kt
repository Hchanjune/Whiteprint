package org.whiteprint.service.auth.application.port.out.event

import org.whiteprint.platform.core.kernel.clientContext.ClientType
import org.whiteprint.platform.core.kernel.clientContext.IP
import org.whiteprint.platform.core.kernel.clientContext.PlatformType
import org.whiteprint.platform.core.messaging.model.event.internal.InternalEvent
import org.whiteprint.service.auth.domain.audit.vo.LoginLogStatus

class LoginCommittedEvent(
    val status: LoginLogStatus,
    val attemptedIdentifier: String,
    val ip: IP,
    val client: ClientType,
    val platform: PlatformType
): InternalEvent {
    override val eventType: String = "login.success"
    override val schemaVersion: String = "Alpha"
}