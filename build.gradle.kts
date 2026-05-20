plugins {
    id("org.springframework.boot")  version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.3.20" apply false
    kotlin("plugin.jpa") version "2.3.20" apply false
    kotlin("plugin.spring") version "2.3.20" apply false

    id("com.google.protobuf") version "0.9.4" apply false
}


allprojects {
    group = "org.whiteprint"
    version = "ALPHA"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}