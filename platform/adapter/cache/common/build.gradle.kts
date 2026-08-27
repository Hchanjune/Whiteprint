/**
 * 캐시 애스펙트의 **스택 무관 공통부**. servlet/reactive 양쪽이 `api` 로 물어 간접으로만 들어간다.
 *
 * ⚠ **소비 서비스가 직접 선언하는 모듈이 아니다.** whiteprint-bom 에 일부러 등재하지 않아서
 * 버전 없이 선언하면 해결되지 않는다(BOM 가드에도 예외로 등록돼 있다).
 * 발행 자체를 막지는 못한다 — servlet/reactive 의 POM 이 이 아티팩트를 가리키므로
 * 발행을 빼면 소비자 쪽 의존성 해결이 깨진다.
 */
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.whiteprint.platform.adapter.cache"

dependencies {
    api(project(":platform:core:kernel"))
    api(project(":platform:core:cache"))

    // 여기 코드는 조인포인트에서 키를 뽑고 빈 메타데이터를 훑는 것뿐이라
    // Redis 도, servlet/reactive 어느 쪽 웹 스택도 필요 없다.
    // 끌어오면 반대편 스택에 불필요한 것이 딸려간다(mongo:common 과 같은 판단).
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
