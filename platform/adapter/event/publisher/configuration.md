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

### build.gradle.kts

```kotlin

implementation(project(":platform:adapter:event:publisher"))

```