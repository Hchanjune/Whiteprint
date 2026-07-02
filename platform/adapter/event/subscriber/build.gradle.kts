plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.messaging"

dependencies {
    api(project(":platform:core:messaging"))
    api(project(":platform:infra:messaging:kafka"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
}

kotlin {
    jvmToolchain(21)
}