package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod

@ConfigurationProperties("platform.adapter.security.entry-points")
data class SecurityVerifierConfigurationProperties(
    val rules: List<Rule> = emptyList(),
) {
    data class Rule(
        var path: String,
        var method: HttpMethod,
        var decision: SecurityDecision,
    )
}