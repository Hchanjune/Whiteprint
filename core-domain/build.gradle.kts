plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.hc.core"
version = "0.0.1-SNAPSHOT"

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.test {
    useJUnitPlatform()
}
