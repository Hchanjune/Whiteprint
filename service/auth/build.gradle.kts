plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.service.auth"
version = "0.0.1-SNAPSHOT"
description = "AuthServer"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":platform:core:kernel"))
    implementation(project(":platform:infra:persistence:jpa"))
    implementation(project(":platform:adapter:web:servlet"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}