plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.messaging"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:messaging"))
    api(project(":platform:infra:persistence:jpa"))
    api(project(":platform:infra:persistence:mongo:servlet"))

    api(project(":platform:infra:observability:servlet"))
    compileOnly("io.projectreactor:reactor-core")
    compileOnly("org.springframework.data:spring-data-mongodb")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
}

kotlin {
    jvmToolchain(21)
}