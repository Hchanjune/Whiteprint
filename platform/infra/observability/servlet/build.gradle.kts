plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.observability"

dependencies {
    api("com.github.Hchanjune.operation-manager-kit:servlet:0.11.3-fix4")
    // infra.observability.tracing.endpoint 가 있으면 OMK span 을 OTLP 로 내보낸다
    api(project(":platform:infra:observability:otel"))

    // 나가는 호출에 trace 헤더를 붙이는 ClientHttpRequestInterceptor
    implementation("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-aspectj")
    testImplementation("io.opentelemetry:opentelemetry-sdk")
    // OMK 자동설정의 MetricsConfiguration 이 MeterRegistry 를 참조한다
    testImplementation("io.micrometer:micrometer-core")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
