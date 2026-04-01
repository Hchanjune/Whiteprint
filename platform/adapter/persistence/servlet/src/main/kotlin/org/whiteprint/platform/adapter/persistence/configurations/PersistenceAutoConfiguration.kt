package org.whiteprint.platform.adapter.persistence.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.persistence.configurations.jpa.JpaConfiguration
import org.whiteprint.platform.adapter.persistence.configurations.jpa.JpaConfigurationProperties

@AutoConfiguration
@Import(JpaConfiguration::class)
@EnableConfigurationProperties(
    PersistenceConfigurationProperties::class,
    JpaConfigurationProperties::class,
)
class PersistenceAutoConfiguration