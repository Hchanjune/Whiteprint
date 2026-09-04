plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.identifier"

dependencies {
    api(project(":platform:core:kernel"))

    compileOnly("org.springframework.boot:spring-boot")

    // spring.factories 등록이 실제로 먹는지까지 부팅으로 확인한다
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // 스프링이 Kotlin 클래스를 인스턴스화할 때 필요하다. 실제 서비스는 Kotlin+Spring 이라 항상 갖고 있고,
    // 이 모듈은 테스트에서만 직접 필요하다.
    testImplementation(kotlin("reflect"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
