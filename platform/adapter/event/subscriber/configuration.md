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

### build.gradle.kts

```kotlin

implementation(project(":platform:adapter:event:subscriber"))

```