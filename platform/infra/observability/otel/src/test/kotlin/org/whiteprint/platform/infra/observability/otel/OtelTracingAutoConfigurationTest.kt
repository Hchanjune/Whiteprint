package org.whiteprint.platform.infra.observability.otel

import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.trace.SdkTracerProvider
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OtelTracingAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(OtelTracingAutoConfiguration::class.java))

    @Test
    fun `엔드포인트가 없으면 Tracer 를 만들지 않는다`() {
        runner.run { context ->
            assertTrue(context.getBeansOfType(Tracer::class.java).isEmpty())
            assertTrue(context.getBeansOfType(SdkTracerProvider::class.java).isEmpty())
        }
    }

    @Test
    fun `엔드포인트가 빈 문자열이어도 만들지 않는다`() {
        // env 치환이 비어 들어오는 dev 형태 — ConditionalOnProperty 였다면 켜졌을 경우
        runner.withPropertyValues("infra.observability.tracing.endpoint=").run { context ->
            assertTrue(context.getBeansOfType(Tracer::class.java).isEmpty())
        }
    }

    @Test
    fun `엔드포인트가 있으면 서비스 이름과 샘플링 비율을 담은 Tracer 를 만든다`() {
        runner.withPropertyValues(
            "infra.observability.tracing.endpoint=http://localhost:4318/v1/traces",
            "infra.observability.tracing.sampling-ratio=0.25",
            "spring.application.name=sample-service",
        ).run { context ->
            assertEquals(1, context.getBeansOfType(Tracer::class.java).size)
            val provider = context.getBean(SdkTracerProvider::class.java)
            // toString 은 SDK 가 공개하는 유일한 설정 조회 경로
            val description = provider.toString()
            assertTrue("service.name=\"sample-service\"" in description, description)
            assertTrue("TraceIdRatioBased{0.250000}" in description, description)
        }
    }

    @Test
    fun `서비스 이름이 없으면 unknown_service 로 내보낸다`() {
        runner.withPropertyValues("infra.observability.tracing.endpoint=http://localhost:4318/v1/traces")
            .run { context ->
                val provider = context.getBean(SdkTracerProvider::class.java)
                assertTrue("service.name=\"unknown_service\"" in provider.toString())
            }
    }
}
