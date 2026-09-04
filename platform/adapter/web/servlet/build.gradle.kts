plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.web"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Cores
    api(project(":platform:core:kernel"))
    // 식별자 노드 번호 배선. 이 어댑터가 이미 TsidGenerator 로 응답 id 를 만들고,
    // 모든 서비스가 web 을 쓰므로 여기 달아두면 소비처가 따로 추가하지 않아도 된다.
    // (web 없는 앱은 adapter:identifier 를 직접 추가해야 노드 번호가 배선된다)
    api(project(":platform:adapter:identifier"))
    api(project(":platform:core:projection"))
    api(project(":platform:infra:observability:servlet"))

    // Spring WebMVC
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework:spring-tx")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-aspectj")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Prometheus
    api("io.micrometer:micrometer-registry-prometheus")

    // OpenTelemetry
    api("io.opentelemetry:opentelemetry-api")
    //api("io.micrometer:micrometer-tracing")
    api("io.micrometer:micrometer-tracing-bridge-otel")
    api("io.opentelemetry:opentelemetry-exporter-otlp")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}