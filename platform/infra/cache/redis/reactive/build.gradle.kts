plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

group = "org.whiteprint.platform.infra.cache"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:cache"))

    implementation("org.apache.commons:commons-pool2")
    api("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
