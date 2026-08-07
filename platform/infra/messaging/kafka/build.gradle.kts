plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

group = "org.whiteprint.platform.infra.messaging.kafka"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:messaging"))

    api(project(":platform:infra:serializer:jackson"))

    api("org.springframework.kafka:spring-kafka")

    implementation("org.slf4j:slf4j-api")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}