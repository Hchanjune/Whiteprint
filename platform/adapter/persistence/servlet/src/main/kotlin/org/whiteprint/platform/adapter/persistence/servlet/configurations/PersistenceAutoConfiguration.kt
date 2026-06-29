package org.whiteprint.platform.adapter.persistence.servlet.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.JpaConfiguration
import org.whiteprint.platform.adapter.persistence.servlet.configurations.jpa.JpaConfigurationProperties
import org.whiteprint.platform.adapter.persistence.servlet.configurations.mongo.MongoConfiguration

@AutoConfiguration
@Import(JpaConfiguration::class, MongoConfiguration::class)
@EnableConfigurationProperties(
    PersistenceConfigurationProperties::class,
    JpaConfigurationProperties::class,
)
class PersistenceAutoConfiguration