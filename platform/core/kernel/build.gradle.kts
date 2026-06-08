plugins {
    kotlin("jvm")
    id("com.google.protobuf")
}

group = "org.whiteprint.platform.core"

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-kotlin:4.28.2")
    api("com.google.protobuf:protobuf-java:4.28.2")
    api("com.google.protobuf:protobuf-java-util:4.28.2")
}



kotlin {
    jvmToolchain(21)
}