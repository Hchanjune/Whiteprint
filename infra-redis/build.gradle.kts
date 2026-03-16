plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

group = "com.hc"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core-kernel"))
    api(project(":core-cache"))

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")


    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}