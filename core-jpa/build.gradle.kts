plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `maven-publish`
}

dependencies {
    api(project(":core-kernel"))
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
