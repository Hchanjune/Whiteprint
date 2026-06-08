plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.core"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":platform:core:kernel"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}