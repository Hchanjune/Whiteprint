package org.whiteprint.platform.infra.observability.servlet

import io.github.hchanjune.omk.core.provider.TelemetryPropagationProvider
import io.github.hchanjune.omk.servlet.Operations
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

/**
 * 지금 열린 OMK span(보통 `@ManagedExternalCall`)을 나가는 요청의 trace 헤더로 싣는다.
 * 받는 서비스의 OMK 필터가 그 헤더를 이어받아 같은 trace 의 자식이 된다.
 *
 * 헤더 형식은 OMK 전파 모드를 따른다(`TelemetryPropagationProvider` — W3C 면 traceparent).
 * 관리 컨텍스트 밖이거나 열린 span 이 없으면 아무것도 붙이지 않는다.
 *
 * ```kotlin
 * RestClient.builder()
 *     .baseUrl(url)
 *     .requestInterceptor(managedTraceHeaderInterceptor)
 *     .build()
 * ```
 */
class ManagedTraceHeaderInterceptor(
    private val propagationProvider: TelemetryPropagationProvider,
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        if (Operations.hasContext) {
            val context = Operations.context
            val span = context.peek()
            if (span != null && context.traceId.isNotBlank()) {
                propagationProvider.inject(context.traceId, span.spanId) { name, value ->
                    request.headers.set(name, value)
                }
            }
        }
        return execution.execute(request, body)
    }
}
