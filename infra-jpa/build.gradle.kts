plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `maven-publish`
}

group = "com.hc.infra"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":core-kernel"))
    api(project(":core-domain"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
