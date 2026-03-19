plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whitepring.platform.adapter.security.verifier"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:infra:observability:servlet"))
    api(project(":platform:adapter:security:verifier:core"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
}

kotlin {
    jvmToolchain(21)
}