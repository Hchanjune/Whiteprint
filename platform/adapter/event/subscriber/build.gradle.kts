plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.event"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:core:messaging"))
    api(project(":platform:infra:messaging:kafka"))
}

kotlin {
    jvmToolchain(21)
}