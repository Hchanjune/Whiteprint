plugins {
    kotlin("jvm")
}

group = "com.hc.infra.security"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core-kernel"))

    api("io.jsonwebtoken:jjwt-api:0.13.0")
    api("io.jsonwebtoken:jjwt-impl:0.13.0")
    api("io.jsonwebtoken:jjwt-jackson:0.13.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}