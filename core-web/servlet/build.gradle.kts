plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.hc"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Cores
    api(project(":core-kernel"))
    api(project(":core-jwt"))

    //OperationManagerKit
    api("com.github.Hchanjune.operation-manager-kit:core:0.4.2")
    api("com.github.Hchanjune.operation-manager-kit:spring-webmvc:0.4.2")

    // Spring WebMVC
    api("org.springframework.boot:spring-boot-starter-web")
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