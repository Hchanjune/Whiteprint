package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.whiteprint.platform.adapter.security.verifier.servlet.configuration.SecurityVerifierConfigurationProperties

class PermittedEntryPointProvider(
    private val props: SecurityVerifierConfigurationProperties,
) {
    fun matchers(): List<RequestMatcher> {
        return props.permittedEntryPoints.map { rule ->
                PathPatternRequestMatcher.pathPattern(rule.method, rule.path)
            }
    }
}