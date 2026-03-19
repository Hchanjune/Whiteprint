package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.springframework.http.HttpMethod
import org.whiteprint.platform.adapter.security.verifier.servlet.configuration.SecurityDecision

data class SecuredEntryPoint(
    val path: String,
    val method: HttpMethod,
    val decision: SecurityDecision,
)
