plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.lock.servlet"

dependencies {
    api(project(":platform:core:lock"))
    implementation(project(":platform:infra:cache:redis:servlet"))
    implementation(project(":platform:infra:observability:servlet"))

    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.apache.commons:commons-pool2")

    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // 애스펙트 동작 실측(가짜 락 연산 — Redis 불필요)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // 실제 스프링 컨텍스트에서 @Transactional·캐시 애스펙트 자리와의 순서 실측
    testImplementation("org.springframework:spring-tx")
    testImplementation(project(":platform:adapter:cache:common"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
