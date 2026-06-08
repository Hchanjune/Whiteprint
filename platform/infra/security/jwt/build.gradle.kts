plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.infra.persistence"

dependencies {
    api(project(":platform:core:security"))

    api("io.jsonwebtoken:jjwt-api:0.13.0")
    api("io.jsonwebtoken:jjwt-impl:0.13.0")
    api("io.jsonwebtoken:jjwt-jackson:0.13.0")
}

kotlin {
    jvmToolchain(21)
}