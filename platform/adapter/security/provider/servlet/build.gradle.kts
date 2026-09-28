plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.security.provider"

dependencies {
    api(project(":platform:core:kernel"))

    // Vault 연결·KMS 빈은 security:kms 가 한 번만 등록한다
    api(project(":platform:adapter:security:kms"))

    api(project(":platform:core:security"))
    api(project(":platform:infra:security:jwt"))

    api(project(":platform:infra:observability:servlet"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
}

kotlin {
    jvmToolchain(21)
}