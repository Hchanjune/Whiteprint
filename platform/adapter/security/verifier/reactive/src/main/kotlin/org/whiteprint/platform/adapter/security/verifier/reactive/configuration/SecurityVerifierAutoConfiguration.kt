package org.whiteprint.platform.adapter.security.verifier.reactive.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration(before = [ReactiveWebSecurityAutoConfiguration::class, ReactiveUserDetailsServiceAutoConfiguration::class])
@Import(
    SecurityCacheConfiguration::class,
    SecurityVerifierConfiguration::class,
    SecurityExceptionHandler::class,
)
@EnableConfigurationProperties(
    SecurityCacheConfigurationProperties::class,
    SecurityVerifierConfigurationProperties::class,
)
class SecurityVerifierAutoConfiguration
