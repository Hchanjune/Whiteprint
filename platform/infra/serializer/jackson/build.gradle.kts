plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.serializer"

dependencies {
    api(project(":platform:core:kernel"))
    api(platform("tools.jackson:jackson-bom:3.1.0"))
    api("tools.jackson.core:jackson-databind")
    // api 로 노출한다 — Redis 캐시 직렬화기가 KotlinModule 을 명시적으로 붙이고,
    // 소비 서비스가 같은 모듈을 다시 선언하지 않아도 되게 한다.
    api("tools.jackson.module:jackson-module-kotlin")

    // JacksonRedisSerializers 용. compileOnly 인 이유: 이 모듈은 Redis 를 쓰지 않고,
    // 그 클래스를 부르는 쪽(캐시 어댑터·보안 verifier)은 이미 spring-data-redis 를 갖고 있다.
    // api 로 내보내면 Redis 를 안 쓰는 소비자에게까지 딸려간다.
    compileOnly("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("com.google.protobuf:protobuf-kotlin:4.28.2")
    implementation("com.google.protobuf:protobuf-java:4.28.2")
    implementation("com.google.protobuf:protobuf-java-util:4.28.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}