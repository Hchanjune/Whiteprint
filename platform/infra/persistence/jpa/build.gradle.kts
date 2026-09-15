plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.persistence"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:domain"))
    api(project(":platform:core:projection"))
    // @FencingGuarded 엔티티 갱신 검사(LockContext 토큰 비교). 거절 예외(LockException)가 소비처로 올라가므로 api.
    api(project(":platform:core:lock"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // 커서 페이지네이션 실측 검증(로컬 Postgres 필요 — 접속 불가 시 자동 skip)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.postgresql:postgresql")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}