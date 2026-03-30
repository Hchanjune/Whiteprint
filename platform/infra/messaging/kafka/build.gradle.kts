plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

group = "org.whiteprint.platform.infra.messaging.kafka"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:messaging"))

    api(project(":platform:infra:serializer:jackson"))

    api("org.springframework.kafka:spring-kafka")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}