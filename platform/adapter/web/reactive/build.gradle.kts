plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.adapter.web"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}