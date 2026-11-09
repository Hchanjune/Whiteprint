package org.whiteprint.platform.infra.observability.reactive

import io.github.hchanjune.omk.core.provider.TelemetryPropagationProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

// TelemetryPropagationProvider 는 OMK 자동설정이 만든다
@AutoConfiguration(afterName = ["io.github.hchanjune.omk.reactive.config.auto.OperationManagerReactiveAutoConfiguration"])
class ManagedTraceHeaderAutoConfiguration {

    @Bean
    @ConditionalOnBean(TelemetryPropagationProvider::class)
    @ConditionalOnMissingBean
    fun managedTraceHeaderInterceptor(propagationProvider: TelemetryPropagationProvider): ManagedTraceHeaderInterceptor =
        ManagedTraceHeaderInterceptor(propagationProvider)
}
