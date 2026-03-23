package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod

@ConfigurationProperties("adapter.security.entry-points")
data class SecurityVerifierConfigurationProperties(
    var cachePrefix: String = "",
    val entryPoints: List<Rule> = emptyList(),
) {
    data class Rule(
        var path: String,
        var method: HttpMethod,
        var decision: SecurityDecision,
    )
}