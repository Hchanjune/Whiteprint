plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.security.verifier"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:infra:serializer:jackson"))

    api(project(":platform:core:security"))
    api(project(":platform:infra:security:jwt"))

    // Vault 연결·KMS 빈은 security:kms 가 한 번만 등록한다
    api(project(":platform:adapter:security:kms"))

    api(project(":platform:infra:observability:servlet"))
    api(project(":platform:infra:cache:redis:servlet"))

    implementation("org.apache.commons:commons-pool2")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
}

kotlin {
    jvmToolchain(21)
}