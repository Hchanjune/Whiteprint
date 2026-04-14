# Security Verifier Configuration Guide (SpringBoot)

This is document for the security verifier adapter. It contains settings and example descriptions.

이 문서는 플랫폼의 보안 어댑터를 사용하기 위한 설정 규격과 설명예시를 포함하고 있습니다.

### application.yml
```yaml
adapter:
  security:
    verifier:
      # 1. Security Verifier Policy 인증 정책 설정
      policy:
        key-alias: access-token-sig
        expected-issuers:
          - Whiteprint
        header-name: Authorization
        scheme: Bearer
      
      # 2. Api EntryPonts Policy 엔트리 포인트 (보안 필터 예외 및 인가 정책)
      permitted-entry-points:
        - path: /api/auth/permit
          method: POST
        - path: /api/v1/permit
          method: POST

      # 3. KMS Config (Recommended Implement: infra:kms:vault)
      kms:
        datasource:
          host: localhost
          port: 8200
          password: ~
          transit-path: transit
        cache:
          expires-after-write-minutes: 60
          maximum-size: 1000

      # 4. Cache For Revocation (Recommended Implement: infra:cache:redis)
      cache:
        datasource:
          host: localhost
          port: 6379
          password: ~
          database: 0
        pooling:
          enabled: true
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait-millis: -1
        timeout:
          command-timeout-millis: 2000
          shutdown-timeout-millis: 100
```

### build.gradle.kts

```kotlin

// For Servlet (MVC)
implementation(project(":platform:adapter:security:verifier:servlet"))

// For Reactive (WebFlux)
implementation(project(":platform:adapter:security:verifier:reactive"))

```