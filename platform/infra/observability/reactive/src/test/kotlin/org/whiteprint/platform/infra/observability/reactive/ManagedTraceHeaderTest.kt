package org.whiteprint.platform.infra.observability.reactive

import io.github.hchanjune.omk.core.bridge.SpanBridge
import io.github.hchanjune.omk.core.context.ManagedContext
import io.github.hchanjune.omk.core.metric.MetricDescriptor
import io.github.hchanjune.omk.core.metric.MetricKind
import io.github.hchanjune.omk.core.metric.MetricLayer
import io.github.hchanjune.omk.core.metric.MetricName
import io.github.hchanjune.omk.core.metric.MetricPolicy
import io.github.hchanjune.omk.core.metric.MetricTags
import io.github.hchanjune.omk.core.provider.SpanIdProvider
import io.github.hchanjune.omk.reactive.ReactiveOperations
import io.github.hchanjune.omk.reactive.async.ManagedContextElement
import io.github.hchanjune.omk.reactive.config.auto.OperationManagerReactiveAutoConfiguration
import io.github.hchanjune.omk.reactive.provider.W3CTelemetryPropagationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import org.whiteprint.platform.infra.observability.otel.OtelTracingAutoConfiguration
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ManagedTraceHeaderTest {

    // OMK 를 일부러 앞에 둔다 — 실제 순서는 @AutoConfiguration(before/after) 가 정해야 한다
    private val runner = ReactiveWebApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                OperationManagerReactiveAutoConfiguration::class.java,
                ManagedTraceHeaderAutoConfiguration::class.java,
                OtelTracingAutoConfiguration::class.java,
            )
        )

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
    fun `IO 디스패처의 ManagedContextElement 안에서는 열린 span 을 traceparent 로 싣는다`() {
        val parent = ManagedContext(spanIdProvider = SpanIdProvider { "parent0000000000" })
            .also { it.injectTraceId(TRACE_ID) }

        val (request, spanId) = runBlocking {
            withContext(Dispatchers.IO + ManagedContextElement(parent)) {
                // IO 스레드에서 @ManagedExternalCall span 을 열고 블로킹 호출하는 흐름
                val forked = ReactiveOperations.threadBoundContextOrNull()!!
                val span = forked.push(
                    name = MetricName("UserClient.get"),
                    kind = MetricKind.TIMER,
                    policy = MetricPolicy.defaults(),
                    tags = MetricTags.empty(),
                    descriptor = MetricDescriptor(layer = MetricLayer.EXTERNAL),
                    idProvider = SpanIdProvider { SPAN_ID },
                )
                intercept() to span.spanId
            }
        }

        assertEquals("00-$TRACE_ID-$spanId-01", request.headers.getFirst("traceparent"))
    }

    @Test
    fun `Reactor 스레드에서 바로 부르면 아무것도 붙이지 않는다`() {
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
