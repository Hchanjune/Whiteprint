package org.whiteprint.platform.adapter.security.verifier.reactive.security

import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import org.whiteprint.platform.adapter.security.verifier.reactive.configuration.SecurityVerifierConfigurationProperties

class PermittedPathProvider(props: SecurityVerifierConfigurationProperties) {

    private data class CompiledRule(val method: HttpMethod, val pattern: PathPattern)

    private val rules: List<CompiledRule> = props.permittedEntryPoints
        .filter { it.path.isNotBlank() && it.method.isNotBlank() }
        .map { CompiledRule(HttpMethod.valueOf(it.method.uppercase()), PathPatternParser().parse(it.path)) }

    fun isPermitted(request: ServerHttpRequest): Boolean =
        rules.any { it.method == request.method && it.pattern.matches(request.path.pathWithinApplication()) }

    fun entries(): List<Pair<HttpMethod, String>> =
        rules.map { it.method to it.pattern.patternString }
}
