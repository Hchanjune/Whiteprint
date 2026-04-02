plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.persistence"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:infra:persistence:jpa"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}