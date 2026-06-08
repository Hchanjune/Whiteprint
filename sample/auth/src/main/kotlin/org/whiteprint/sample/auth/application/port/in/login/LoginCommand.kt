package org.whiteprint.sample.auth.application.port.`in`.login

import org.whiteprint.platform.core.kernel.clientContext.ClientContext

data class LoginCommand(
    val identifier: String,
    val rawPassword: String,
    val clientContext: ClientContext,
)