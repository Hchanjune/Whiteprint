plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.persistence"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:infra:persistence:jpa"))
    api(project(":platform:infra:persistence:mongo:servlet"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
}