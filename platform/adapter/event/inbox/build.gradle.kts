plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.messaging"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:messaging"))
    api(project(":platform:infra:persistence:jpa"))

    api(project(":platform:infra:observability:servlet"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
    jvmToolchain(21)
}