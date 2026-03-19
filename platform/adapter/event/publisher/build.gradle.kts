plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.messaging"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:messaging"))
    api(project(":platform:infra:messaging:kafka"))

    implementation("org.springframework:spring-tx")
    implementation("org.springframework:spring-context")
}

kotlin {
    jvmToolchain(21)
}