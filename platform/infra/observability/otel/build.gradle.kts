/**
 * OMK span 을 OTLP 로 내보내는 OTel SDK `Tracer` 빈. servlet/reactive 가 `api` 로 물어 간접으로만 들어간다.
 *
 * Boot 의 tracing 자동설정(`spring-boot-starter-opentelemetry`)은 일부러 쓰지 않는다.
 * 그걸 켜면 SCG Observation 이 게이트웨이의 traceparent 를 덮어써 OMK traceId 와 갈라지고,
 * servlet 서버 span 이 Boot·OMK 로 중복되며, OTLP 메트릭 push 까지 켜진다(2026-11-06 실측).
 * 여기서는 SDK 로 Tracer 하나만 만들어 OMK 브릿지(OtelSpanBridge)가 OMK span 만 내보내게 한다.
 *
 * ⚠ 소비 서비스가 직접 선언하는 모듈이 아니다 — BOM 가드의 internalOnly 에 등록돼 있다.
 */
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.observability"

dependencies {
    // OMK 브릿지가 찾는 빈 타입(io.opentelemetry.api.trace.Tracer)
    api("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
