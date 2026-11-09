package org.whiteprint.platform.infra.observability.servlet

import io.github.hchanjune.omk.core.bridge.SpanBridge
import io.github.hchanjune.omk.core.context.ManagedContext
import io.github.hchanjune.omk.core.metric.MetricDescriptor
import io.github.hchanjune.omk.core.metric.MetricKind
import io.github.hchanjune.omk.core.metric.MetricLayer
import io.github.hchanjune.omk.core.metric.MetricName
import io.github.hchanjune.omk.core.metric.MetricPolicy
import io.github.hchanjune.omk.core.metric.MetricTags
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import io.github.hchanjune.omk.servlet.Operations
import io.github.hchanjune.omk.servlet.config.auto.OperationManagerServletAutoConfiguration
import io.github.hchanjune.omk.servlet.provider.W3CTelemetryPropagationProvider
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import org.whiteprint.platform.infra.observability.otel.OtelTracingAutoConfiguration
import java.net.URI
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ManagedTraceHeaderTest {

    // OMK 를 일부러 앞에 둔다 — 실제 순서는 @AutoConfiguration(before/after) 가 정해야 한다
    private val runner = WebApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                OperationManagerServletAutoConfiguration::class.java,
                ManagedTraceHeaderAutoConfiguration::class.java,
                OtelTracingAutoConfiguration::class.java,
            )
        )

    @AfterTest
    fun clear() = Operations.clear()

    @Test
    fun `엔드포인트가 있으면 OMK 브릿지가 Tracer 를 찾아 켜진다`() {
        runner.withPropertyValues("infra.observability.tracing.endpoint=http://localhost:4318/v1/traces")
            .run { context ->
                assertTrue(context.getBeansOfType(SpanBridge::class.java).isNotEmpty(), "OMK SpanBridge 가 없다 — 자동설정 순서 확인")
                assertTrue(context.getBeansOfType(ManagedTraceHeaderInterceptor::class.java).isNotEmpty())
            }
    }

    @Test
    fun `엔드포인트가 없으면 브릿지 없이 OMK 만 동작하고 인터셉터는 그대로 있다`() {
        runner.run { context ->
            assertTrue(context.getBeansOfType(SpanBridge::class.java).isEmpty())
            assertTrue(context.getBeansOfType(ManagedTraceHeaderInterceptor::class.java).isNotEmpty())
        }
    }

    @Test
    fun `열린 span 을 traceparent 로 싣는다`() {
        val context = ManagedContext(spanIdProvider = SpanIdProvider { SPAN_ID }).also { it.injectTraceId(TRACE_ID) }
        context.push(
            name = MetricName("UserClient.get"),
            kind = MetricKind.TIMER,
            policy = MetricPolicy.defaults(),
            tags = MetricTags.empty(),
            descriptor = MetricDescriptor(layer = MetricLayer.EXTERNAL),
            idProvider = SpanIdProvider { SPAN_ID },
        )
        Operations.initialize(context)

        val request = intercept()

        assertEquals("00-$TRACE_ID-$SPAN_ID-01", request.headers.getFirst("traceparent"))
    }

    @Test
    fun `관리 컨텍스트 밖이면 아무것도 붙이지 않는다`() {
        assertNull(intercept().headers.getFirst("traceparent"))
    }

    @Test
    fun `열린 span 이 없으면 아무것도 붙이지 않는다`() {
        Operations.initialize(ManagedContext(spanIdProvider = SpanIdProvider { SPAN_ID }).also { it.injectTraceId(TRACE_ID) })
        assertNull(intercept().headers.getFirst("traceparent"))
    }

    private fun intercept(): MockClientHttpRequest {
        val request = MockClientHttpRequest(HttpMethod.GET, URI.create("http://user-query/users/1"))
        ManagedTraceHeaderInterceptor(W3CTelemetryPropagationProvider())
            .intercept(request, ByteArray(0), ClientHttpRequestExecution { _, _ -> MockClientHttpResponse() })
        return request
    }

    companion object {
        const val TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736"
        const val SPAN_ID = "00f067aa0ba902b7"
    }
}
