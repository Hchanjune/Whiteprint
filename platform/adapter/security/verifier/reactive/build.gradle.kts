plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.adapter.security.verifier"

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}