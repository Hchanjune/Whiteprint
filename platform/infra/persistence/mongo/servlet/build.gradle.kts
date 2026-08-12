plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.persistence"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:projection"))
    api(project(":platform:infra:persistence:mongo:common"))
    api("org.springframework.boot:spring-boot-starter-data-mongodb")
}

kotlin {
    jvmToolchain(21)
}
