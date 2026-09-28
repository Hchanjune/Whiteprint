plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.security.kms"

dependencies {
    api(project(":platform:core:kms"))
    api(project(":platform:infra:kms:vault"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.slf4j:slf4j-api")
}

kotlin {
    jvmToolchain(21)
}
