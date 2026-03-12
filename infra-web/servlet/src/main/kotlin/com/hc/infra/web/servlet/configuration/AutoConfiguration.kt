package com.hc.infra.web.servlet.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import com.hc.infra.web.servlet.security.SecurityConfiguration
import com.hc.infra.web.servlet.security.SecurityConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(
    SecurityConfiguration::class,
    JacksonConfig::class,
)
@EnableConfigurationProperties(SecurityConfigurationProperties::class)
class AutoConfiguration