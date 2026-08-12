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