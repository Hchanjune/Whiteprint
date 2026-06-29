plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.core"

dependencies {
    implementation(project(":platform:core:kernel"))
}

kotlin {
    jvmToolchain(21)
}