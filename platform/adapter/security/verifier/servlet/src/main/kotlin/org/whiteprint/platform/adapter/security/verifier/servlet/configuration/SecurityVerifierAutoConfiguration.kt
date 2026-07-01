package org.whiteprint.platform.adapter.security.verifier.servlet.configuration


import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration(before = [SecurityAutoConfiguration::class, ServletWebSecurityAutoConfiguration::class, UserDetailsServiceAutoConfiguration::class])
@Import(
    SecurityVerifierKmsConfiguration::class,
    SecurityCacheConfiguration::class,
    SecurityVerifierConfiguration::class,
    SecurityExceptionHandler::class,
    SecurityAuthorizerConfiguration::class,
)
@EnableConfigurationProperties(
    SecurityCacheConfiguration::class,
    SecurityVerifierConfigurationProperties::class,
    SecurityVerifierKmsConfigurationProperties::class,
)
class SecurityVerifierAutoConfiguration