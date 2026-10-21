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
      # 서명 키 정책. KMS(Vault) 연결은 adapter.security.kms 에 둔다 — ../kms/configuration.md
      key-policy:
        access-token:
          key-alias: access-token-sig
          rotation-interval-seconds: 2592000
          overlap-seconds: 86400
          algorithm: RSA_2048
        refresh-token:
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

### 서명 키 (생성·회전)
- **기동 시 생성**: `access-token.key-alias` · `refresh-token.key-alias` 키가 Vault 에 없으면 `algorithm` 으로 만든다(`adapter:security:kms` 의 managed key).
  이미 있는데 종류가 다르면 기동을 멈춘다 — 키를 덮어쓰지 않는다.
- **알고리즘은 `RSA_2048` · `RSA_4096` 만** 받는다. 서명은 Vault `pkcs1v15`, 검증은 `SHA256withRSA` 로 고정이라 다른 종류는 기동 시 거부한다.
  (예전 예시의 `HMAC_SHA256` 은 헤더의 `alg` 만 `HS256` 으로 바꾸고 실제 서명은 RSA 였다.)
- **회전**: 최신 버전이 만들어진 지 `rotation-interval-seconds` 가 지나면 새 버전을 만든다(1시간마다 확인, 0 이하면 회전 안 함).
  기준이 Vault 의 버전 생성 시각이라 재기동해도 주기가 초기화되지 않는다. 락이 없어 인스턴스가 여럿이면 드물게 버전이 하나 더 생길 수 있다(둘 다 유효).
- **옛 버전은 지우지 않는다** — 토큰 헤더의 `ver` 로 검증하므로 회전 전 토큰은 만료될 때까지 그대로 통과한다.
  `overlap-seconds` 는 아직 읽는 곳이 없다. 옛 버전 정리를 넣는다면 토큰 최대 수명(refresh `expiration-seconds`)보다 길어야 한다.
- 서명은 헤더에 적은 버전으로 고정된다 — 헤더를 만든 직후 키가 회전해도 헤더·서명 버전이 어긋나지 않는다.
- Vault 토큰에 transit 키 생성(`transit/keys/*` write)·회전(`transit/keys/*/rotate`) 권한이 필요하다.
