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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
            artifactId = "whiteprint-bom"
        }
    }
}
