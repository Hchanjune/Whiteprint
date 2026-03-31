package org.whiteprint.platform.adapter.persistence.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.persistence.configuration.jpa.JpaConfiguration
import org.whiteprint.platform.adapter.persistence.configuration.jpa.JpaConfigurationProperties

@AutoConfiguration
@Import(JpaConfiguration::class)
@EnableConfigurationProperties(
    PersistenceConfigurationProperties::class,
    JpaConfigurationProperties::class,
)
class PersistenceAutoConfiguration