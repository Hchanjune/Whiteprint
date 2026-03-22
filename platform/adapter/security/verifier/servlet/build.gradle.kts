plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whitepring.platform.adapter.security.verifier"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:infra:serializer:jackson"))

    api(project(":platform:core:security"))
    api(project(":platform:infra:security:jwt"))

    api(project(":platform:core:kms"))
    api(project(":platform:infra:kms:vault"))

    api(project(":platform:infra:observability:servlet"))
    api(project(":platform:infra:cache:redis"))

    implementation("org.apache.commons:commons-pool2")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
}

kotlin {
    jvmToolchain(21)
}