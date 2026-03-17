package org.whiteprint.platform.adapter.web.servlet.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.whiteprint.platform.adapter.web.servlet.security.SecurityConfiguration
import org.whiteprint.platform.adapter.web.servlet.security.SecurityConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(
    _root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.SecurityConfiguration::class,
    JacksonConfig::class,
)
@EnableConfigurationProperties(_root_ide_package_.org.whiteprint.platform.adapter.web.servlet.security.SecurityConfigurationProperties::class)
class AutoConfiguration