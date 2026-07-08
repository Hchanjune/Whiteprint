plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.persistence"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:domain"))
    api(project(":platform:core:projection"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")
}

kotlin {
    jvmToolchain(21)
}