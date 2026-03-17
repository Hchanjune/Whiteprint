plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

group = "com.hc.infra.kafka"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core:kernel"))
    api(project(":core:messaging"))

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}