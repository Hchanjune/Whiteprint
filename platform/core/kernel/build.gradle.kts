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

    // 식별자 생성기 실측 검증용(스프링 컨텍스트는 쓰지 않는다 — 커널은 순수 모듈로 남는다)
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}



kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
