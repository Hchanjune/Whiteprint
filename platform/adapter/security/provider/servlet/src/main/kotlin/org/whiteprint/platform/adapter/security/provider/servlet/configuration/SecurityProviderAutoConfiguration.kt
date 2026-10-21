package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration
@EnableScheduling
@Import(
    SecurityProviderConfiguration::class,
    SecurityProviderSignerConfiguration::class,
)
@EnableConfigurationProperties(
    SecurityProviderConfigurationProperties::class,
    SecurityProviderKeyPolicyConfigurationProperties::class,
)
class SecurityProviderAutoConfiguration