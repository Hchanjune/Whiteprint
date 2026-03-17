plugins {
    kotlin("jvm")
    id("com.google.protobuf")
}

group = "com.hc.core"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(platform("tools.jackson:jackson-bom:3.1.0"))
    api("tools.jackson.core:jackson-databind")
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation("com.google.protobuf:protobuf-kotlin:4.28.2")
    implementation("com.google.protobuf:protobuf-java:4.28.2")
    implementation("com.google.protobuf:protobuf-java-util:4.28.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("kotlin")
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}