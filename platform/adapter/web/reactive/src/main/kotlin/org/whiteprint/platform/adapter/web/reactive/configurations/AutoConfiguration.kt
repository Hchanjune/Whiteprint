package org.whiteprint.platform.adapter.web.reactive.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.whiteprint.platform.adapter.web.reactive.request.ClientContextResolver

@AutoConfiguration
@Import(PlatformExceptionHandler::class)
class AutoConfiguration {

    @Bean
    fun clientContextResolver(): ClientContextResolver = ClientContextResolver()
}
