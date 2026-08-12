plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.infra.persistence"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:projection"))

    // 드라이버 없는 spring-data-mongodb만 쓴다 — 여기 코드는 Criteria/Query/Sort 조립뿐이라
    // 동기/리액티브 어느 쪽 드라이버도 필요 없고, 끌어오면 반대편 스택에 불필요한 드라이버가 딸려간다.
    api("org.springframework.data:spring-data-mongodb")
}

kotlin {
    jvmToolchain(21)
}
