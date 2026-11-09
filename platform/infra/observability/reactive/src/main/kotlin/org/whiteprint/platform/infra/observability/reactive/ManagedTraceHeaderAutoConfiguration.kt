package org.whiteprint.platform.infra.observability.reactive

import io.github.hchanjune.omk.core.provider.TelemetryPropagationProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

// TelemetryPropagationProvider 는 OMK 자동설정이 만든다.
// OMK 와 같은 스택 조건을 건다 — adapter-event-inbox 때문에 리액티브 앱에도 servlet 모듈이 함께 들어오므로,
// 조건이 없으면 양쪽이 같은 이름의 빈을 등록해 기동이 실패한다.
@AutoConfiguration(afterName = ["io.github.hchanjune.omk.reactive.config.auto.OperationManagerReactiveAutoConfiguration"])
@ConditionalOnClass(name = ["org.springframework.web.reactive.DispatcherHandler"])
class ManagedTraceHeaderAutoConfiguration {

    @Bean
    @ConditionalOnBean(TelemetryPropagationProvider::class)
    @ConditionalOnMissingBean
    fun managedTraceHeaderInterceptor(propagationProvider: TelemetryPropagationProvider): ManagedTraceHeaderInterceptor =
        ManagedTraceHeaderInterceptor(propagationProvider)
}
