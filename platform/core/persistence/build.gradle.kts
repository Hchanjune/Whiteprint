plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "org.whiteprint.platform.core"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:core:kernel"))
    api("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.test {
    useJUnitPlatform()
}
