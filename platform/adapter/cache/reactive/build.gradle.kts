plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whitepring.platform.adapter.cache.reactive"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}