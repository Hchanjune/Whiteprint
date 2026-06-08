plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.core"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:kernel"))
}

kotlin {
    jvmToolchain(21)
}