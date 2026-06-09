plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.lock"

dependencies {
    api(project(":platform:core:cache"))
    implementation(project(":platform:infra:cache:redis"))

    implementation("org.springframework.boot:spring-boot-starter-aspectj")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
}

kotlin {
    jvmToolchain(21)
}