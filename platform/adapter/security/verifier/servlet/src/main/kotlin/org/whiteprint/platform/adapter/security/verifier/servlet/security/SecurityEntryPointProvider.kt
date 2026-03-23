package org.whiteprint.platform.adapter.security.verifier.servlet.security

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.whiteprint.platform.adapter.security.verifier.servlet.configuration.SecurityDecision
import org.whiteprint.platform.adapter.security.verifier.servlet.configuration.SecurityVerifierConfigurationProperties

class SecurityEntryPointProvider(
    private val props: SecurityVerifierConfigurationProperties,
) {
    fun permitAllMatchers(): List<RequestMatcher> {
        return props.entryPoints.filter { it.decision == SecurityDecision.PERMIT }
            .map { rule ->
                PathPatternRequestMatcher.pathPattern(rule.method, rule.path)
            }
    }

    fun denyAllMatchers(): List<RequestMatcher> {
        return props.entryPoints.filter { it.decision == SecurityDecision.DENY }
            .map { rule ->
                PathPatternRequestMatcher.pathPattern(rule.method, rule.path)
            }
    }

    fun authenticateAllMatchers(): List<RequestMatcher> {
        return props.entryPoints.filter { it.decision == SecurityDecision.AUTHENTICATED }
            .map { rule ->
                PathPatternRequestMatcher.pathPattern(rule.method, rule.path)
            }
    }
}