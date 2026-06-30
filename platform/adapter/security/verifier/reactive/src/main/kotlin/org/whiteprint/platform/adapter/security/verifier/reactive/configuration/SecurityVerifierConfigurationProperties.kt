package org.whiteprint.platform.adapter.security.verifier.reactive.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod

@ConfigurationProperties("adapter.security.verifier")
data class SecurityVerifierConfigurationProperties(
    var policy: AccessTokenVerifierPolicy = AccessTokenVerifierPolicy(),
    var permittedEntryPoints: MutableList<Rule> = mutableListOf(),
) {

    data class AccessTokenVerifierPolicy(
        var keyAlias: String = "access-token-sig",
        var expectedIssuers: List<String> = listOf("Whiteprint", "Sample-Auth"),
        var headerName: String = "Authorization",
        var scheme: String = "Bearer",
        var revocation: Revocation = Revocation()
    )

    data class Revocation(
        var accountRevocationMillis: Long = 604800000,
    )

    data class Rule(
        var path: String,
        var method: HttpMethod,
    )
}
