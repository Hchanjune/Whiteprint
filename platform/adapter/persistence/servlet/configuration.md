# Persistence (Servlet) Configuration Guide (SpringBoot)

This is document for the persistence adapter. It contains settings and example descriptions.

이 문서는 플랫폼의 영속(데이터베이스) 어댑터를 사용하기 위한 설정 규격과 설명예시를 포함하고 있습니다.

### application.yml (Full Settings)
```yaml
adapter:
  persistence:
    infrastructure-implementation: jpa
    jpa: 
      datasource:
        database: postgresql
        driver-class-name: ~ #Automatically Configured. Skip Recommended
        host: localhost
        port: 5432
        database-name: mydatabase
        parameters:
          param1: "123"
          param2: "456"
        username: 
        password:
      hikari:
        maximum-pool-size: 10
        minimum-idle: 10
        connection-timeout-millis: 30000
        idle-timeout-millis: 600000
        max-lifetime-millis: 1800000
        auto-commit: false
        pool-name: myhikaripool
      hibernate:
        fetch-size: 1000
        batch-size: 100
        provider-disables-autocommit: true
        generate-statistics: false
        batch-versioned-data: true
        order-inserts: true
        order-updates: true
        format-sql: true
        highlight-sql: true
        ddl-auto: none
        default-batch-fetch-size: 100
      options:
        entity-packages-to-scan:
          - a
          - b
          - c
        show-sql: false
        generate-ddl: false
```

### build.gradle.kts
```kotlin
implementation(project(":platform:adapter:persistence:servlet"))
```

### Requirements
```kotlin
@SpringBootApplication
@EnableJpaRepositories
class Application
```