package org.whiteprint.platform.infra.observability.otel

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * OMK span 전용 OTel SDK Tracer.
 *
 * OMK 의 OtelBridgeConfiguration 은 `@ConditionalOnBean(Tracer)` 라서 **OMK 자동설정보다 먼저**
 * 평가돼야 한다. 순서가 뒤집히면 브릿지가 조용히 꺼지고 span 이 하나도 나가지 않는다(에러 없음).
 *
 * 전역 `GlobalOpenTelemetry` 에는 등록하지 않는다. OMK 브릿지 말고 다른 계측이 이 SDK 를 집어
 * span 을 만드는 일이 없게 하기 위해서다.
 */
@AutoConfiguration(
    beforeName = [
        "io.github.hchanjune.omk.servlet.config.auto.OperationManagerServletAutoConfiguration",
        "io.github.hchanjune.omk.reactive.config.auto.OperationManagerReactiveAutoConfiguration",
    ]
)
@ConditionalOnExpression("'\${${TracingProperties.PREFIX}.endpoint:}' != ''")
@EnableConfigurationProperties(TracingProperties::class)
class OtelTracingAutoConfiguration {

    // close() 가 BatchSpanProcessor 를 flush 한다 — 종료 직전 span 이 버려지지 않게
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    fun omkSdkTracerProvider(
        properties: TracingProperties,
        @Value("\${spring.application.name:unknown_service}") serviceName: String,
    ): SdkTracerProvider {
        val exporter = OtlpHttpSpanExporter.builder()
            .setEndpoint(requireNotNull(properties.endpoint))
            .build()
        return SdkTracerProvider.builder()
            .setResource(
                Resource.getDefault().merge(
                    Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), serviceName))
                )
            )
            .setSampler(Sampler.parentBased(Sampler.traceIdRatioBased(properties.samplingRatio)))
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .build()
    }

    @Bean
    @ConditionalOnMissingBean(Tracer::class)
    fun omkTracer(tracerProvider: SdkTracerProvider): Tracer =
        tracerProvider.get(INSTRUMENTATION_SCOPE)

    companion object {
        const val INSTRUMENTATION_SCOPE = "org.whiteprint.omk"
    }
}
