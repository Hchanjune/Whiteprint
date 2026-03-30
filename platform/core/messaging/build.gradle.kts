plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.core"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":platform:core:kernel"))
}

kotlin {
    jvmToolchain(21)
}