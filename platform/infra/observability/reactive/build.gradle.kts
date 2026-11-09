plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.observability"

dependencies {
    api("com.github.Hchanjune.operation-manager-kit:reactive:0.11.3-fix4")
    // infra.observability.tracing.endpoint 가 있으면 OMK span 을 OTLP 로 내보낸다
    api(project(":platform:infra:observability:otel"))

    // 나가는 호출에 trace 헤더를 붙이는 ClientHttpRequestInterceptor (RestClient 는 spring-web)
    implementation("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-aspectj")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("io.opentelemetry:opentelemetry-sdk")
    // OMK 자동설정의 MetricsConfiguration 이 MeterRegistry 를 참조한다
    testImplementation("io.micrometer:micrometer-core")
    // adapter-event-inbox 가 servlet 모듈을 api 로 물어서 리액티브 서비스에도 함께 들어온다 — 같은 구성으로 검증
    testImplementation(project(":platform:infra:observability:servlet"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
