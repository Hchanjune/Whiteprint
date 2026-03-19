plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.infra.serializer"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":platform:core:kernel"))
    api(platform("tools.jackson:jackson-bom:3.1.0"))
    api("tools.jackson.core:jackson-databind")
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation("com.google.protobuf:protobuf-kotlin:4.28.2")
    implementation("com.google.protobuf:protobuf-java:4.28.2")
    implementation("com.google.protobuf:protobuf-java-util:4.28.2")
}

kotlin {
    jvmToolchain(21)
}