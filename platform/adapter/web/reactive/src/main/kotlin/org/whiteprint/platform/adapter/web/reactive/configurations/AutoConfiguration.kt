package org.whiteprint.platform.adapter.web.reactive.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.whiteprint.platform.adapter.web.reactive.binding.CursorDirectionConverter
import org.whiteprint.platform.adapter.web.reactive.binding.SortDirectionConverter
import org.whiteprint.platform.adapter.web.reactive.binding.SortableFieldConverterFactory
import org.whiteprint.platform.adapter.web.reactive.request.ClientContextResolver

@AutoConfiguration
@Import(PlatformExceptionHandler::class)
class AutoConfiguration {

    @Bean
    fun clientContextResolver(): ClientContextResolver = ClientContextResolver()

    /** projection query 파라미터(sortBy/sortDirection/direction)의 wire name 바인딩. */
    @Bean
    fun projectionQueryBindingConfigurer(): WebFluxConfigurer = object : WebFluxConfigurer {
        override fun addFormatters(registry: FormatterRegistry) {
            registry.addConverterFactory(SortableFieldConverterFactory())
            registry.addConverter(SortDirectionConverter())
            registry.addConverter(CursorDirectionConverter())
        }
    }
}
