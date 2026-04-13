# Security Provider Configuration Guide (SpringBoot)

This is document for the security provider adapter. It contains settings and example descriptions.

이 문서는 플랫폼의 보안 어댑터를 사용하기 위한 설정 규격과 설명예시를 포함하고 있습니다.

### application.yml
```yaml
adapter:
  security:
    provider:
      token:
        access-token-policy:
          issuer: Sample
          expiration-seconds: 3600
        refresh-token-policy:
          issuer: Sample
          expiration-seconds: 3600
          cookie-header: refresh
      kms:
        datasource:
          host: localhost
          port: 8200
          password: root
          transit-path: transit
        access-token-key-policy:
          key-alias: access-token-sig
          rotation-interval-seconds: 2592000
          overlap-seconds: 86400
          algorithm: RSA_2048
        refresh-token-key-policy:
          key-alias: refresh-token-sig
          rotation-interval-seconds: 2592000
          overlap-seconds: 86400
          algorithm: RSA_2048
```

### build.gradle.kts

```kotlin

// For Servlet Only (MVC)
implementation(project(":platform:adapter:security:provider:servlet"))

```