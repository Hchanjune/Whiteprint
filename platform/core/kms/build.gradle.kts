plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.core"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:kernel"))
}

kotlin {
    jvmToolchain(21)
}