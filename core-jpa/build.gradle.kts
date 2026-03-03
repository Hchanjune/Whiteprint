plugins {
    kotlin("jvm")
    kotlin("plugin.jpa") version "2.2.21"
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
