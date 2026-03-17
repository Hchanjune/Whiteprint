package org.whiteprint.platform.adapter.web.servlet.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod

@ConfigurationProperties("security.entry-point")
data class SecurityConfigurationProperties(
    val rules: List<Rule> = emptyList(),
) {
    data class Rule(
        var path: String,
        var method: HttpMethod,
        var decision: org.whiteprint.platform.adapter.web.servlet.security.SecurityDecision,
    )
}
