plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.adapter.security"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:adapter:security:verifier"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}