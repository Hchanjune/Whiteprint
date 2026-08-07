# Event Subscriber Configuration Guide (SpringBoot)

This is document for the event subscriber adapter. It contains settings and example descriptions.

이 문서는 플랫폼의 이벤트 구독 인터페이스를 사용하기 위한 설정 규격과 설명예시를 포함하고 있습니다.

### application.yml
```yaml
adapter:
  event:
    subscriber:
    infrastructure-implementation: kafka
    kafka:
      datasource:
        host: localhost
        port: 9092
      consumer:
        group-id: platform-group
        auto-offset-reset: earliest
        concurrency: 3
        enable-auto-commit: false
        isolation-level: read_committed
      subscription-policy:
        prefix: test
        version: v1
        separator: .
        event-types:
          - user.created
          - user.deleted
      error-handling:
        retry:
          max-attempts: 3
          backoff-interval: 2000
        dead-letter:
          enabled: true
          topic-suffix: .DLT
          producer:
            acks: all
            batch-size: 16384
            linger-millis: 5
            compression-type: lz4
            retries: 2147483647
```

### subscription-policy.event-types

구독할 이벤트 타입 목록. 각 항목은 `prefix{separator}{eventType}{separator}version` 규칙으로
토픽명이 만들어진다(위 예시 → `test.user.created.v1`).

**빈 목록(`[]`)이어도 기동된다.** 이 경우 Kafka 리스너 컨테이너를 아예 만들지 않고
경고 로그만 남긴 뒤 넘어간다 — 아직 수신할 이벤트가 없는 서비스를 위한 것이다.
반대로 말하면 **핸들러(`AbstractEventHandler`)를 만들어 놨는데 여기에 해당 이벤트 타입을
빠뜨리면 아무 에러 없이 조용히 아무것도 수신하지 않는다.** 핸들러를 추가할 때 항상 같이 채울 것.

### 구독자는 토픽을 만들지 않는다

컨슈머에 `allow.auto.create.topics=false` 가 강제된다(클라이언트 기본값은 `true`).
토픽 스펙(파티션 수·retention)의 주인은 **발행 서비스의 `topic-policy`** 이고, 구독자가 먼저 떠서
브로커 기본값(보통 1파티션)으로 만들어버리면 나중에 발행 서비스가
`TopicExistsException` 으로 스킵해 선언한 스펙이 영영 적용되지 않기 때문이다.

따라서 아직 없는 토픽을 구독하면 `UNKNOWN_TOPIC_OR_PARTITION` 경고가 반복해서 찍힌다.
**정상이다** — 발행 서비스가 기동해 토픽을 만들면 자동으로 붙는다.

### build.gradle.kts

```kotlin

implementation(project(":platform:adapter:event:subscriber"))

```