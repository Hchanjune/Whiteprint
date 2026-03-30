plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `maven-publish`
}

group = "org.whiteprint.platform.infra.persistence"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:domain"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")
}

kotlin {
    jvmToolchain(21)
}