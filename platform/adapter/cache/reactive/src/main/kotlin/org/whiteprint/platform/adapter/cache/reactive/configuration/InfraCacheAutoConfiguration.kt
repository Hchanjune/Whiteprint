package org.whiteprint.platform.adapter.cache.reactive.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(CacheConfiguration::class)
class InfraCacheAutoConfiguration
