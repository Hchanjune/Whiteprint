# Event Publisher Configuration Guide (SpringBoot)

This is document for the event publisher adapter. It contains settings and example descriptions.

이 문서는 플랫폼의 이벤트 발행 인터페이스를 사용하기 위한 설정 규격과 설명예시를 포함하고 있습니다.

### application.yml
```yaml
adapter:
  event:
    publisher:
      infrastructure-implementation: kafka
      kafka:
        datasource:
          host: localhost
          port: 9092
        topic-policy:
          auto-create: true
          prefix: wp
          separator: .
          version: v1
          topics:
            "[user.created]":
              partitions: 3
              replication-factor: 1
              retention-millis: 604800000
              cleanup-policy: delete
            "[user.deleted]":
              partitions: 3
              replication-factor: 1
              retention-millis: 604800000
              cleanup-policy: delete
        producer:
          acks: all
          batch-size: 16384
          linger-millis: 5
          compression-type: lz4
          retries: 2147483647
        publish:
          retry:
            max-attempts: 3
            backoff-interval: 2000
```

### topic-policy.topics

발행을 허용할 이벤트 타입 목록이자 토픽 스펙. 토픽명은
`prefix{separator}{eventType}{separator}version` 규칙으로 만들어진다(위 예시 → `wp.user.created.v1`).

이 맵은 **화이트리스트로도 동작한다.** 여기에 없는 `eventType` 을 EXTERNAL 로 발행하면
`TOPIC_NOT_CONFIGURED` 가 나고 해당 outbox 행이 FAILED 로 마킹된다.

`auto-create: true` 면 기동 시점에 AdminClient 로 선언된 토픽을 생성한다
(`datasource` 의 브로커 주소를 그대로 쓴다 — `spring.kafka.*` 설정은 보지 않는다).
이미 있는 토픽은 건너뛰고, **브로커에 접속하지 못해도 경고만 남기고 기동은 계속된다**
(접속 확인이 필요하면 `connection-validation.enabled: true`).

**빈 맵(`{}`)이어도 기동된다.** 토픽 생성도 하지 않는다 — 아직 외부로 발행할 이벤트가
없는 서비스를 위한 것이다.

### build.gradle.kts

```kotlin

implementation(project(":platform:adapter:event:publisher"))

```