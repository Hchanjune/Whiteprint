package org.whiteprint.platform.adapter.security.verifier.reactive.security

import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.util.pattern.PathPatternParser
import org.whiteprint.platform.adapter.security.verifier.reactive.configuration.SecurityVerifierConfigurationProperties

class PermittedPathProvider(
    private val props: SecurityVerifierConfigurationProperties,
) {
    private val parser = PathPatternParser()

    fun isPermitted(request: ServerHttpRequest): Boolean {
        return props.permittedEntryPoints.any { rule ->
            val pattern = parser.parse(rule.path)
            request.method == rule.method &&
                pattern.matches(request.path.pathWithinApplication())
        }
    }

    fun entries(): List<Pair<HttpMethod, String>> =
        props.permittedEntryPoints.map { it.method to it.path }
}
