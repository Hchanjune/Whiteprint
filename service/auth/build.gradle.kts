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
    implementation(project(":platform:adapter:web:servlet"))
    implementation(project(":platform:adapter:security:verifier:servlet"))
    implementation(project(":platform:adapter:security:provider:servlet"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}