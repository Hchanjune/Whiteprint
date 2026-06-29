package org.whiteprint.platform.adapter.persistence.reactive.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.persistence.reactive.configurations.mongo.MongoConfiguration

@AutoConfiguration
@Import(MongoConfiguration::class)
class PersistenceAutoConfiguration
