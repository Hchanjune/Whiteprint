plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whitepring.platform.adapter.serializer"
version = "0.0.1-SNAPSHOT"
dependencies {
    api(project(":platform:infra:serializer:jackson"))
    api(project(":platform:infra:serializer:protobuf"))

    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
    jvmToolchain(21)
}