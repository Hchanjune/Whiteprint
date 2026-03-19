plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.adapter.security.verifier"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:core:kernel"))

    api("io.jsonwebtoken:jjwt-api:0.13.0")
    api("io.jsonwebtoken:jjwt-impl:0.13.0")
    api("io.jsonwebtoken:jjwt-jackson:0.13.0")
}

kotlin {
    jvmToolchain(21)
}