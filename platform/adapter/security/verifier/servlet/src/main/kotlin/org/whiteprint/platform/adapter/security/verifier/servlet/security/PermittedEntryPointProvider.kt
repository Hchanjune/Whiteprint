package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.springframework.http.HttpMethod
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.whiteprint.platform.adapter.security.verifier.servlet.configuration.SecurityVerifierConfigurationProperties

class PermittedEntryPointProvider(props: SecurityVerifierConfigurationProperties) {

    private val matchers: List<RequestMatcher> = props.permittedEntryPoints
        .filter { it.path.isNotBlank() && it.method.isNotBlank() }
        .map { PathPatternRequestMatcher.pathPattern(HttpMethod.valueOf(it.method.uppercase()), it.path) }

    fun matchers(): List<RequestMatcher> = matchers
}