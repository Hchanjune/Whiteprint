plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.security.provider"

dependencies {
    api(project(":platform:core:kernel"))

    api(project(":platform:core:kms"))
    api(project(":platform:infra:kms:vault"))

    api(project(":platform:core:security"))
    api(project(":platform:infra:security:jwt"))

    api(project(":platform:infra:observability:servlet"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
}

kotlin {
    jvmToolchain(21)
}