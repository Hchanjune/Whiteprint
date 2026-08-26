plugins {
    kotlin("jvm")
}

group = "org.whiteprint.platform.infra.serializer"

dependencies {
    api(project(":platform:core:kernel"))
    api(platform("tools.jackson:jackson-bom:3.1.0"))
    api("tools.jackson.core:jackson-databind")
    // api 로 노출한다 — Redis 캐시 직렬화기가 KotlinModule 을 명시적으로 붙이고,
    // 소비 서비스가 같은 모듈을 다시 선언하지 않아도 되게 한다.
    api("tools.jackson.module:jackson-module-kotlin")

    implementation("com.google.protobuf:protobuf-kotlin:4.28.2")
    implementation("com.google.protobuf:protobuf-java:4.28.2")
    implementation("com.google.protobuf:protobuf-java-util:4.28.2")
}

kotlin {
    jvmToolchain(21)
}