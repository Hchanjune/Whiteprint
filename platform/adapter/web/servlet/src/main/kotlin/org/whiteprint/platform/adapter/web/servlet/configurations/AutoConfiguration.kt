package org.whiteprint.platform.adapter.web.servlet.configurations

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.whiteprint.platform.adapter.web.servlet.binding.CursorDirectionConverter
import org.whiteprint.platform.adapter.web.servlet.binding.SortDirectionConverter
import org.whiteprint.platform.adapter.web.servlet.binding.SortableFieldConverterFactory
import org.whiteprint.platform.adapter.web.servlet.request.ClientContextResolver

@AutoConfiguration
@Import(
    PlatformExceptionHandler::class
)
class AutoConfiguration {

    @Bean
    fun clientContextResolver(): ClientContextResolver = ClientContextResolver()

    /** projection query 파라미터(sortBy/sortDirection/direction)의 wire name 바인딩. */
    @Bean
    fun projectionQueryBindingConfigurer(): WebMvcConfigurer = object : WebMvcConfigurer {
        override fun addFormatters(registry: FormatterRegistry) {
            registry.addConverterFactory(SortableFieldConverterFactory())
            registry.addConverter(SortDirectionConverter())
            registry.addConverter(CursorDirectionConverter())
        }
    }

}
