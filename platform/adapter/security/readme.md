# Security Architecture
# 보안 아키텍처

## Overview / 개요

본 시스템은 JWT 기반의 Access/Refresh Token 이중 토큰 체계를 사용하며, Redis 캐시를 활용한 토큰 상태 관리와 권한 갱신 메커니즘을 제공합니다.
This system uses a dual JWT token scheme (Access/Refresh) with Redis cache-based token status management and a permission update mechanism.

## Token Structure / 토큰 구조

### Access Token

요청 인증에 사용되는 단기 토큰입니다. subject, audience, permissions 정보를 claims에 포함합니다.
A short-lived token used for request authentication. Contains subject, audience, and permissions in its claims.

### Refresh Token

Access Token 재발급에 사용되는 장기 토큰입니다. subject, audience, permissions 정보를 claims에 포함하여 DB 조회 없이 토큰 재발급이 가능합니다.
A long-lived token used to reissue Access Tokens. Contains subject, audience, and permissions in its claims, enabling token reissuance without DB lookup.

## Request Authentication (Spring Security Filter) / 요청 인증 (Spring Security 필터)

### StatelessSecurityFilter

`OncePerRequestFilter`를 확장한 Stateless 인증 필터입니다. 모든 요청에 대해 Access Token을 검증하며, DispatcherServlet 이전에 동작합니다.
A stateless authentication filter extending `OncePerRequestFilter`. Validates the Access Token on every request, operating before the DispatcherServlet.

처리 흐름:
Processing flow:

1. `PermittedEntryPointProvider`가 제공하는 matcher 목록과 비교하여 permit-all 대상이면 필터를 건너뜁니다.
   Checks against matchers from `PermittedEntryPointProvider` — if the request is permit-all, the filter is skipped.

2. 요청 헤더에서 scheme(e.g. Bearer) prefix를 제거하고 Access Token을 추출합니다. 토큰이 없으면 `TOKEN_NOT_FOUND` 예외를 설정합니다.
   Extracts the Access Token from the request header after stripping the scheme prefix (e.g. Bearer). If absent, sets a `TOKEN_NOT_FOUND` exception.

3. `AccessTokenVerifier.verifyOrThrow()`로 서명 및 만료를 검증합니다.
   Validates signature and expiration via `AccessTokenVerifier.verifyOrThrow()`.

4. 검증 성공 시 claims의 permissions를 `SimpleGrantedAuthority`로 변환하여 `VerifiedUser` 인증 객체를 생성하고, `SecurityContextHolder`에 등록합니다.
   On success, converts claims permissions to `SimpleGrantedAuthority`, creates a `VerifiedUser` authentication object, and registers it in the `SecurityContextHolder`.

### Exception Handling in Filter / 필터 내 예외 처리

필터는 DispatcherServlet 이전에 동작하므로 `@ControllerAdvice`에 도달하지 않습니다. 따라서 예외를 직접 던지지 않고 request attribute(`SECURITY_EXCEPTION`)에 저장한 뒤 filter chain을 계속 진행합니다.
Since the filter operates before the DispatcherServlet, exceptions cannot reach `@ControllerAdvice`. Instead of throwing, exceptions are stored in a request attribute (`SECURITY_EXCEPTION`) and the filter chain continues.

`SecurityException`은 `SecurityPolicy` enum 기반으로 구조화되며, 예상치 못한 예외는 `TOKEN_VERIFICATION_INTERNAL_ERROR`로 래핑됩니다.
`SecurityException` is structured around a `SecurityPolicy` enum. Unexpected exceptions are wrapped as `TOKEN_VERIFICATION_INTERNAL_ERROR`.

### SecurityAuthenticationEntryPoint

인증되지 않은 요청(SecurityContext에 인증 객체가 없는 상태)이 보호된 엔드포인트에 도달하면 Spring Security가 `AuthenticationEntryPoint`를 호출합니다.
When an unauthenticated request (no authentication in SecurityContext) reaches a protected endpoint, Spring Security invokes the `AuthenticationEntryPoint`.

request attribute에서 `SecurityException`을 꺼내 `ApiResponse.error()` 형태의 JSON 응답을 직접 작성합니다. attribute가 없으면 `TOKEN_NOT_FOUND`를 기본값으로 사용합니다.
Retrieves the `SecurityException` from the request attribute and writes a JSON response in `ApiResponse.error()` format. Defaults to `TOKEN_NOT_FOUND` if no attribute is present.

이 구조를 통해 필터 레벨의 보안 예외도 애플리케이션의 표준 에러 응답 형식(`ApiResponse`)과 일관된 포맷으로 클라이언트에 전달됩니다.
This ensures that filter-level security exceptions are delivered to the client in the same standard error response format (`ApiResponse`) as application-level exceptions.

## Token Lifecycle / 토큰 생명주기

### Token Issuance / 토큰 발급

`TokenProvider`가 `AccessTokenProfile`과 `RefreshTokenProfile`을 기반으로 각각의 토큰을 생성합니다.
`TokenProvider` generates tokens based on `AccessTokenProfile` and `RefreshTokenProfile` respectively.

### Token Verification / 토큰 검증

`RefreshTokenVerifier.verifyOrThrow()`가 토큰의 서명과 만료를 검증합니다. 상태 검증(revocation, force update)은 서비스 레이어에서 별도로 수행합니다.
`RefreshTokenVerifier.verifyOrThrow()` validates the token's signature and expiration. Status checks (revocation, force update) are performed separately at the service layer.

### Refresh Rotation / 리프레시 로테이션

Refresh 요청 시 사용된 Refresh Token은 즉시 `REFRESH_ROTATION` 사유로 revoke되며, 잔여 수명만큼의 TTL로 캐시에 등록됩니다. 새로운 Access Token과 Refresh Token이 함께 발급됩니다.
Upon refresh, the used Refresh Token is immediately revoked with `REFRESH_ROTATION` reason and registered in cache with a TTL equal to its remaining lifetime. A new Access Token and Refresh Token are issued together.

## Token Status Management / 토큰 상태 관리

### Token Revocation / 토큰 폐기

`TokenRevoker`가 개별 토큰을 revoke하며, 캐시 키 `security:revocation:token:{tokenId}`에 저장됩니다. TTL은 해당 토큰의 잔여 수명으로 설정되어 만료 후 자동 정리됩니다.
`TokenRevoker` revokes individual tokens, stored at cache key `security:revocation:token:{tokenId}`. TTL is set to the token's remaining lifetime for automatic cleanup after expiration.

### Force Update / 강제 업데이트

권한 변경 시 `AccountTokenStatusManager.setForceUpdate(subject, updatedAt)`를 호출하여 캐시 키 `security:force-update:{subject}`에 변경 시점(epoch millis)을 저장합니다.
When permissions change, `AccountTokenStatusManager.setForceUpdate(subject, updatedAt)` is called, storing the change timestamp (epoch millis) at cache key `security:force-update:{subject}`.

Refresh 요청 시 `checkForceUpdate(subject, issuedAt)`로 토큰 발급 시점과 비교하여, 토큰이 권한 변경 이전에 발급된 경우에만 DB에서 최신 권한을 조회합니다.
During refresh, `checkForceUpdate(subject, issuedAt)` compares the token's issuance time — only tokens issued before the permission change trigger a DB lookup for the latest permissions.

Force Update 플래그의 TTL은 Refresh Token 정책의 최대 수명(`accountRevocationMillis`)으로 설정됩니다. 이 시간이 지나면 모든 Refresh Token이 만료되어 재인증이 필요하므로 플래그도 의미를 잃습니다.
The Force Update flag's TTL is set to the Refresh Token policy's maximum lifetime (`accountRevocationMillis`). After this period, all Refresh Tokens expire requiring re-authentication, making the flag irrelevant.

## Multi-Device Support / 다중 디바이스 지원

Force Update 플래그는 특정 디바이스의 refresh 이후에도 삭제되지 않습니다. 각 디바이스가 자연스럽게 refresh를 수행할 때 `issuedAt < updatedAt` 비교를 통해 개별적으로 갱신됩니다.
The Force Update flag is not cleared after a specific device's refresh. Each device is individually updated through `issuedAt < updatedAt` comparison when it naturally performs a refresh.

이미 갱신된 디바이스의 새 토큰은 `issuedAt`이 `updatedAt` 이후이므로 조건에 걸리지 않아 불필요한 DB 조회가 발생하지 않습니다.
Tokens from already-updated devices have `issuedAt` after `updatedAt`, so they don't match the condition and avoid unnecessary DB lookups.

## Request Authentication Flow / 요청 인증 흐름

```
1. HTTP Request arrives at StatelessSecurityFilter
   HTTP 요청이 StatelessSecurityFilter에 도달

2. Check permit-all matchers (PermittedEntryPointProvider)
   인증 제외 경로 확인 (PermittedEntryPointProvider)
   → If matched: skip filter, continue chain
     매칭 시: 필터 건너뜀, 체인 계속

3. Extract Access Token from Authorization header
   Authorization 헤더에서 Access Token 추출
   → If absent: set TOKEN_NOT_FOUND in request attribute
     부재 시: request attribute에 TOKEN_NOT_FOUND 설정

4. AccessTokenVerifier.verifyOrThrow() — signature & expiration check
   서명 및 만료 검증
   → If failed: set SecurityException in request attribute
     실패 시: request attribute에 SecurityException 설정

5. On success: create VerifiedUser, set SecurityContext
   성공 시: VerifiedUser 생성, SecurityContext 설정

6. Continue filter chain → DispatcherServlet
   필터 체인 계속 → DispatcherServlet

7. If no authentication in SecurityContext at protected endpoint:
   보호된 엔드포인트에서 인증 객체가 없으면:
   → SecurityAuthenticationEntryPoint returns ApiResponse.error() JSON
     SecurityAuthenticationEntryPoint가 ApiResponse.error() JSON 반환
```

## Refresh Flow / 리프레시 흐름

```
1. Client sends Refresh Token
   클라이언트가 Refresh Token 전송

2. RefreshTokenVerifier.verifyOrThrow() — signature & expiration check
   서명 및 만료 검증

3. TokenRevoker.revokeToken() — revoke used token (REFRESH_ROTATION)
   사용된 토큰 폐기 (REFRESH_ROTATION)

4. AccountTokenStatusManager.checkForceUpdate(subject, issuedAt)
   강제 업데이트 필요 여부 확인

5-a. Force Update required (issuedAt < updatedAt):
     → Load permissions from DB via AccountRepository
     강제 업데이트 필요 시: DB에서 최신 권한 조회

5-b. No Force Update:
     → Reuse permissions from Refresh Token claims
     강제 업데이트 불필요 시: claims의 권한 재사용

6. Issue new Access Token + Refresh Token with resolved permissions
   확인된 권한으로 새 Access Token + Refresh Token 발급
```

## Permission Change Flow / 권한 변경 흐름

```
1. Permission change occurs in any service
   임의의 서비스에서 권한 변경 발생

2. setForceUpdate(subject, System.currentTimeMillis())
   Force Update 플래그 설정 (현재 시점 기록)

3. Each device's next refresh triggers DB permission reload
   각 디바이스의 다음 refresh 시 DB에서 권한 재조회

4. Flag expires naturally via TTL (= max Refresh Token lifetime)
   플래그는 TTL에 의해 자연 만료 (= Refresh Token 최대 수명)
```

## Key Components / 주요 컴포넌트

| Component / 컴포넌트 | Responsibility / 책임 |
|---|---|
| `StatelessSecurityFilter` | Access Token 추출 및 검증, SecurityContext 설정 / Extracts and verifies Access Token, sets SecurityContext |
| `SecurityAuthenticationEntryPoint` | 인증 실패 시 표준 에러 응답 반환 / Returns standard error response on authentication failure |
| `PermittedEntryPointProvider` | 인증 제외 경로 matcher 제공 / Provides matchers for permit-all paths |
| `AccessTokenVerifier` | Access Token 서명 및 만료 검증 / Validates Access Token signature and expiration |
| `RefreshTokenVerifier` | Refresh Token 서명 및 만료 검증 / Validates Refresh Token signature and expiration |
| `TokenProvider` | Access/Refresh Token 생성 / Generates Access/Refresh Tokens |
| `TokenRevoker` | 개별 토큰 폐기 (캐시 등록) / Revokes individual tokens (cache registration) |
| `AccountTokenStatusManager` | 계정 단위 Force Update 플래그 설정 및 확인 / Sets and checks account-level Force Update flags |
| `SecurityCacheKeyStrategy` | 캐시 키 생성 전략 (서비스 prefix 지원) / Cache key generation strategy (with service prefix support) |

## Cache Key Structure / 캐시 키 구조

| Key Pattern / 키 패턴 | Purpose / 용도 | TTL |
|---|---|---|
| `{prefix}:security:revocation:token:{tokenId}` | 토큰 폐기 기록 / Token revocation record | 토큰 잔여 수명 / Token's remaining lifetime |
| `{prefix}:security:force-update:{subject}` | 강제 업데이트 플래그 / Force update flag | `accountRevocationMillis` |