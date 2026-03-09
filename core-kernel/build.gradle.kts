plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {

    api("org.jetbrains.kotlin:kotlin-reflect")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.1")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
