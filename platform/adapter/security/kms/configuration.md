# Security KMS Configuration Guide (SpringBoot)

This is document for the security KMS adapter. It registers a single Vault connection and the KMS beans
(`KeyOperations`, `KeyMaterialProvider`, `KeyAdminOperations`, `KeyCache`) shared by the security provider,
the security verifier, and any service-level encryption (e.g. encrypting data at rest).

이 문서는 플랫폼의 KMS 어댑터 설정 규격과 설명예시를 포함하고 있습니다.
Vault 연결과 KMS 빈을 **한 번만** 등록하고, 토큰 발급(provider)·토큰 검증(verifier)·서비스 자체 암호화가 함께 씁니다.

### application.yml
```yaml
adapter:
  security:
    kms:
      # 1. Vault 연결 (Recommended Implement: infra:kms:vault)
      datasource:
        host: localhost
        port: 8200
        password: ~          # Vault 토큰
        transit-path: transit
      # 2. 공개키 자료 캐시 — 검증할 때마다 Vault 를 부르지 않도록
      cache:
        expires-after-write-minutes: 60
        maximum-size: 1000
```

### build.gradle.kts

```kotlin
// provider·verifier 가 이미 api 로 끌어오므로, 그 둘을 쓰는 서비스는 따로 추가하지 않아도 된다.
// 보안 어댑터 없이 암호화만 필요할 때 직접 추가한다.
implementation(project(":platform:adapter:security:kms"))
```

### Notes
- 모든 빈이 `@ConditionalOnMissingBean` 이다 — 서비스가 같은 타입을 등록하면 그쪽이 쓰인다.
- 연결은 blocking `VaultTemplate` 하나라 servlet·reactive 구분이 없다.
- 기동 시 `sys/health` 로 연결을 검사하고, 실패하면 기동을 멈춘다.
- 이전 버전의 `adapter.security.verifier.kms` · `adapter.security.provider.kms.datasource` 는 더 이상 읽지 않는다.
  옮기지 않으면 host 가 비어 기동 시 연결 검사에서 실패한다.
