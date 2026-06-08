plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.infra.kms"

dependencies {
    api(project(":platform:core:kms"))
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    api("org.springframework.vault:spring-vault-core:4.0.1")
}

kotlin {
    jvmToolchain(21)
}