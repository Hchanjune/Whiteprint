plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.cache.servlet"

dependencies {
    api(project(":platform:infra:cache:redis:servlet"))
    api(project(":platform:adapter:cache:common"))

    // [CAC] 스팬을 열려면 OMK 의 컨텍스트·SpanSupport 가 필요하다.
    api(project(":platform:infra:observability:servlet"))

    implementation("org.apache.commons:commons-pool2")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}