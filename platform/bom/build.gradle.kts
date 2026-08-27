plugins {
    `java-platform`
    `maven-publish`
}

dependencies {
    constraints {
        // core
        api("com.github.Hchanjune.Whiteprint:core-kernel:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-observability:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-domain:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-cache:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-messaging:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-kms:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-security:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-lock:${project.version}")
        api("com.github.Hchanjune.Whiteprint:core-projection:${project.version}")

        // adapter
        api("com.github.Hchanjune.Whiteprint:adapter-serializer:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-persistence-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-persistence-reactive:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-event-outbox:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-event-inbox:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-event-publisher:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-event-subscriber:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-cache-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-cache-reactive:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-lock-distributed-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-security-provider-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-security-provider-reactive:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-security-verifier-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-security-verifier-reactive:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-web-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:adapter-web-reactive:${project.version}")

        // infra
        api("com.github.Hchanjune.Whiteprint:infra-persistence-jpa:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-persistence-mongo-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-persistence-mongo-reactive:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-cache-redis-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-cache-redis-reactive:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-messaging-kafka:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-observability-servlet:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-observability-reactive:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-client-armeria:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-serializer-jackson:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-serializer-protobuf:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-security-jwt:${project.version}")
        api("com.github.Hchanjune.Whiteprint:infra-kms-vault:${project.version}")
    }
}


/**
 * BOM 누락 방지 가드.
 *
 * 모듈을 새로 만들고 여기 등재를 잊으면 **아무 데서도 안 터진다** — 소비자가 그 모듈을 직접 선언하는
 * 순간에야 "버전 없음"으로 실패하기 때문이다. 그때는 이미 배포된 뒤다.
 * 실제로 `infra-persistence-mongo-*` 3종과 `core-lock`/`core-projection` 이 그렇게 빠져 있었다.
 *
 * 그래서 설정 시점에 대조해서 즉시 실패시킨다. 기준은 루트 빌드의 artifactId 규칙과 같다
 * (`:platform:` 을 떼고 `:` 를 `-` 로).
 */
run {
    val whiteprintGroup = "com.github.Hchanjune.Whiteprint"

    val constrained = configurations["api"].dependencyConstraints
        .filter { it.group == whiteprintGroup }
        .map { it.name }
        .toSet()

    /**
     * BOM 에 **일부러 넣지 않는** 모듈. 소비자가 직접 선언하는 물건이 아니라
     * servlet/reactive 를 통해 transitive 로만 들어가야 하는 내부 모듈이다.
     *
     * BOM 에서 빼두면 소비자가 버전 없이 선언할 수 없다 — 완전한 차단은 아니지만
     * (좌표를 알고 버전을 박으면 여전히 가능) "이건 직접 쓰는 게 아니다" 라는 신호로는 충분하다.
     * 발행 자체를 막을 수는 없다. servlet/reactive 의 POM 이 이 아티팩트를 가리키므로
     * 발행하지 않으면 소비자 쪽 의존성 해결이 깨진다.
     */
    val internalOnly = setOf(
        "adapter-cache-common",
        "infra-persistence-mongo-common",
    )

    // buildFile 존재 여부로 "실제 모듈"만 고른다 — `include("a:b:c")` 는 중간 경로에
    // 빌드 파일 없는 팬텀 프로젝트(:platform:adapter 등)도 만들어낸다.
    val modules = rootProject.subprojects
        .filter { it.path.startsWith(":platform:") && it.path != project.path && it.buildFile.exists() }
        .map { it.path.removePrefix(":platform:").replace(":", "-") }
        .toSet()

    val missing = (modules - constrained - internalOnly).sorted()
    val stale = (constrained - modules - internalOnly).sorted()

    check(missing.isEmpty() && stale.isEmpty()) {
        buildString {
            appendLine("whiteprint-bom 과 실제 모듈 목록이 어긋납니다.")
            if (missing.isNotEmpty()) {
                appendLine("  BOM 에 없는 모듈 (constraints 에 추가하세요):")
                missing.forEach { appendLine("    api(\"$whiteprintGroup:$it:\${project.version}\")") }
            }
            if (stale.isNotEmpty()) {
                appendLine("  모듈이 없는데 BOM 에만 남은 항목 (삭제하세요): ${stale.joinToString(", ")}")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
            artifactId = "whiteprint-bom"
        }
    }
}
