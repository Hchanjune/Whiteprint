plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.adapter.security.provider"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:adapter:security:verifier:core"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}