plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.core.lock"

dependencies {
    implementation(project(":platform:core:kernel"))
}

kotlin {
    jvmToolchain(21)
}
