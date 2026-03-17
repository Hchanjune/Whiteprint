package org.whiteprint.platform.adapter.web.servlet.security

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

class SecurityEntryPointProvider(
    private val props: org.whiteprint.platform.adapter.web.servlet.security.SecurityConfigurationProperties,
) {
    fun permitAllMatchers(): List<RequestMatcher> {
        return props.rules.filter { it.decision == _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.SecurityDecision.PERMIT }
            .map { rule ->
                PathPatternRequestMatcher.pathPattern(rule.method, rule.path)
            }
    }

    fun denyAllMatchers(): List<RequestMatcher> {
        return props.rules.filter { it.decision == _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.SecurityDecision.DENY }
            .map { rule ->
                PathPatternRequestMatcher.pathPattern(rule.method, rule.path)
            }
    }

    fun authenticateAllMatchers(): List<RequestMatcher> {
        return props.rules.filter { it.decision == _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.SecurityDecision.AUTHENTICATED }
            .map { rule ->
                PathPatternRequestMatcher.pathPattern(rule.method, rule.path)
            }
    }
}