plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.core"

dependencies {
    api(project(":platform:core:kernel"))
    api("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.test {
    useJUnitPlatform()
}
