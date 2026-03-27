package org.whiteprint.platform.adapter.security.verifier.servlet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod

@ConfigurationProperties("adapter.security.verifier")
data class SecurityVerifierConfigurationProperties(
    var policy: AccessTokenVerifierPolicy = AccessTokenVerifierPolicy(),
    var entryPoints: MutableList<Rule> = mutableListOf(),
) {

    data class AccessTokenVerifierPolicy(
        var keyAlias: String = "access-token-sig",
        var expectedIssuers: List<String> = listOf("Whiteprint", "Sample-Auth")
    )

    data class Rule(
        var path: String,
        var method: HttpMethod,
        var decision: SecurityDecision,
    )
}