package org.whiteprint.platform.infra.observability.otel

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * OMK span 내보내기 설정.
 *
 * ```yaml
 * infra.observability.tracing:
 *   endpoint: http://alloy.alloy.svc:4318/v1/traces
 *   sampling-ratio: 1.0
 * ```
 */
@ConfigurationProperties(prefix = TracingProperties.PREFIX)
data class TracingProperties(
    /**
     * OTLP HTTP traces 엔드포인트(`/v1/traces` 까지 포함). 비어 있으면 Tracer 빈을 만들지 않아
     * OMK 는 지금처럼 로그·메트릭만 남긴다 — 수집기가 없는 dev 에서 exporter 가 연결 실패를 반복하지 않게.
     */
    var endpoint: String? = null,
    /**
     * 새로 시작하는 trace 의 샘플링 비율(0.0~1.0). 부모가 있는 span 은 부모의 결정을 따른다.
     */
    var samplingRatio: Double = 1.0,
) {
    companion object {
        const val PREFIX = "infra.observability.tracing"
    }
}
