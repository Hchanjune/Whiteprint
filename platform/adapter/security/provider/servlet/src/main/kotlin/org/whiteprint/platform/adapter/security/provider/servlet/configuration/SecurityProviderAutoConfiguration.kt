package org.whiteprint.platform.adapter.security.provider.servlet.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(
    SecurityProviderConfiguration::class,
    SecurityProviderKmsConfiguration::class,
)
@EnableConfigurationProperties(
    SecurityProviderConfigurationProperties::class,
    SecurityProviderKmsConfigurationProperties::class,
)
class SecurityProviderAutoConfiguration