plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.lock.servlet"

dependencies {
    api(project(":platform:core:lock"))
    implementation(project(":platform:infra:cache:redis"))

    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.apache.commons:commons-pool2")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
}

kotlin {
    jvmToolchain(21)
}