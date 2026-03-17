plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.hc.infra.jpa"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core:messaging"))
    api(project(":infra:persistence:jpa"))

    api("com.github.Hchanjune.operation-manager-kit:spring-webmvc:0.5.5")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}