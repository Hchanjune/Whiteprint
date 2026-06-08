plugins {
    kotlin("jvm")
    id("com.google.protobuf")
}

group = "org.whiteprint.platform.infra.serializer"

dependencies {
    api(project(":platform:core:kernel"))
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