plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.messaging"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":platform:core:messaging"))
    api(project(":platform:infra:persistence:jpa"))
    api(project(":platform:infra:persistence:mongo:servlet"))

    api(project(":platform:infra:observability:servlet"))
    compileOnly("io.projectreactor:reactor-core")
    compileOnly("org.springframework.data:spring-data-mongodb")

    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // PARTITION_ORDERED 동시성 실측 검증(로컬 Postgres/Mongo 필요 — 접속 불가 시 자동 skip)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.postgresql:postgresql")
    testImplementation("org.springframework.data:spring-data-mongodb")
    testImplementation("org.mongodb:mongodb-driver-sync")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}