plugins {
    kotlin("jvm")
}

group = "org.whitepring.platform.adapter.security.verifier"
version = "0.0.1-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}