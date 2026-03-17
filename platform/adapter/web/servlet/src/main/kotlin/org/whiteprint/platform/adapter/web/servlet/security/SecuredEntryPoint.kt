package org.whiteprint.platform.adapter.web.servlet.security

import org.springframework.http.HttpMethod

data class SecuredEntryPoint(
    val path: String,
    val method: HttpMethod,
    val decision: SecurityDecision,
)
