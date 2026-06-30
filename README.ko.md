# Whiteprint

**이벤트 기반·CQRS 마이크로서비스를 위한 플랫폼 라이브러리 — 모든 서비스가 똑같은 분산 시스템 기반 코드를 반복해서 만드는 일을 멈추게 하기 위해 만들었습니다.**

[English README](./README.md)

## 왜 Whiteprint인가

분산 시스템을 만들다 보면 결국 모든 서비스가 비슷한 몇 가지 어려운 문제 — 안정적인 메시징, 영속성, 인증, 캐시, 관측성 — 를 조금씩 다른 방식으로, 여기저기 흩어진 채 반복해서 풀게 됩니다. Whiteprint는 이런 반복되는 기반 코드를 하나의 일관된 **플랫폼 레이어**로 모아, 그 위에서 일하는 팀이 매번 같은 횡단 관심사를 다시 풀고 다시 흩뜨리는 대신 비즈니스 로직에 집중할 수 있도록 만들어졌습니다.

## 아키텍처

Whiteprint는 **Hexagonal Architecture** 위에서, 단순한 컨벤션이 아니라 모듈 단위로 강제되는 3계층 분리를 따릅니다:

```
┌────────────────────────────────────┐
│             서비스 (Service)          │   비즈니스 로직만
├────────────────────────────────────┤
│           platform / core           │   프레임워크 독립적 계약(포트)
├────────────────────────────────────┤
│          platform / adapter         │   Spring Boot 연결
├────────────────────────────────────┤
│           platform / infra          │   Kafka · Redis · JPA · Vault · …
└────────────────────────────────────┘
```

- **`core`** — 순수한 도메인 추상화와 계약. 프레임워크 의존성이 전혀 없으며, "어떻게"가 아니라 "무엇을 제공하는지"만 정의합니다.
- **`adapter`** — `core`의 계약을 Spring Boot(자동 구성, 프로퍼티 바인딩, 빈 등록)와 연결하면서도, 프레임워크의 디테일이 도메인으로 새어 들어가지 않도록 막습니다.
- **`infra`** — 특정 기술명이 등장하는 유일한 계층. Kafka, Redis, JPA, Vault 등에 대한 `core` 계약의 실제 구현체입니다.

서비스는 Kafka나 Redis가 아니라 `core`의 추상화에 의존하기 때문에, 인프라를 교체하는 작업은 기존 계약에 맞춰 새로운 `infra` 모듈을 작성하는 것으로 끝나며 비즈니스 로직은 건드릴 필요가 없습니다.

## 모듈 구성

**`platform/core`** — 프레임워크 독립적 계약

| 모듈 | 설명 |
| --- | --- |
| `core:kernel` | `Identifiable`, `Auditable`, `LifeCycle` 등 공통 베이스 계약과 정책/예외 컨벤션 |
| `core:domain` | Aggregate 기반 도메인 모델링 기본 요소 |
| `core:projection` | 읽기 모델 — `ProjectionModel<ID>` (version/soft-delete/감사 필드 내장) |
| `core:cache` | 캐시 계약 |
| `core:lock` | 분산 락 계약 — `LockKey`, `LockHandle`, `DistributedLockOperations`, `@DistributedLock`/`@DistributedLockKey` |
| `core:messaging` | 이벤트, 발행/구독, Outbox/Inbox 계약 |
| `core:kms` | 키 자료, 캐싱, 로테이션 정책 계약 |
| `core:security` | JWT/토큰 검증, 클레임, revocation 계약 및 선언적 권한 어노테이션(`@RequirePermission`, `@ForbidPermission`, `@RequireAnyPermission`, `@RequireAllPermissions`, `@RequireHigherPermissionThan`) |

**`platform/adapter`** — Spring Boot 연결 (서블릿·리액티브 듀얼 스택)

| 모듈 | 설명 |
| --- | --- |
| `adapter:serializer` | 직렬화 포트 연결 |
| `adapter:persistence:servlet` | JPA 영속성 연결 — `JpaRepositoryRegistrar`가 `adapter.persistence.jpa.options.entity-packages-to-scan`에 선언된 패키지를 동적으로 스캔; 사용 서비스에서 `@EnableJpaRepositories` 불필요 |
| `adapter:persistence:reactive` | Reactive MongoDB 영속성 연결 — `ReactiveMongoRepositoryRegistrar`가 `adapter.persistence-reactive.reactive-mongo.options.repository-packages-to-scan`을 동적으로 스캔; `@EnableReactiveMongoRepositories` 불필요 |
| `adapter:event:outbox` / `:inbox` | Outbox/Inbox 폴링 및 멱등성 처리 연결 — `adapter.event.outbox/inbox.infrastructure-implementation`으로 JPA·MongoDB 백엔드 선택 가능; MongoDB 백엔드는 context에 `MongoTemplate`/`ReactiveMongoTemplate` 중 무엇이 있는지에 따라 자동 선택 — 폴링은 항상 `ScheduledThreadPoolExecutor`에서 실행되므로 reactive 서비스에서도 `.block()` 안전; `JpaAutoConfigurationGuard`가 JPA를 사용하지 않는 서비스에서 Spring Boot JPA/JDBC 자동 구성을 자동 제외 |
| `adapter:event:publisher` | 이벤트 발행 연결 — `@EnableScheduling` 자동 적용; 사용 서비스에서 별도 선언 불필요 |
| `adapter:event:subscriber` | 이벤트 구독 연결 — `@EnableScheduling` 자동 적용; 사용 서비스에서 별도 선언 불필요 |
| `adapter:cache:servlet` / `:reactive` | 스택별 캐시 연결 |
| `adapter:lock:distributed:servlet` | 분산 락 AOP 연결 (서블릿 스택) |
| `adapter:security:provider:servlet` / `:reactive` | 토큰 발급(로그인/리프레시) 연결 |
| `adapter:security:verifier:servlet` | 토큰 검증 및 필터 체인 연결 (서블릿) — `@Before` AOP, `SecurityContextSupport.getCurrentClaims()`, `Authorizer` 빈 |
| `adapter:security:verifier:reactive` | 토큰 검증 및 필터 체인 연결 (리액티브) — `@Around` AOP로 `Mono`/`Flux` 래핑, `SecurityContextSupport.currentClaims(): Mono<AccessTokenClaims>` |
| `adapter:web:servlet` | 웹 레이어 컨벤션 — 예외 처리, 응답 래핑; `ResponseEntityGenerator`가 `Operations.context.traceId`(thread-local)로 traceId 읽음 |
| `adapter:web:reactive` | WebFlux 웹 레이어 컨벤션 — `ResponseEntityGenerator.generateInstantData()`가 `Mono<ResponseEntity<ApiResponse<T>>>` 반환, Reactor Context에서 `ReactiveOperations`를 통해 traceId 읽음; `PlatformExceptionHandler`가 WebFlux 전용 예외(`WebExchangeBindException`, `ServerWebInputException`, `ResponseStatusException` 등) 처리 |

**`platform/infra`** — 실제 기술 구현체

| 모듈 | 설명 |
| --- | --- |
| `infra:persistence:jpa` | 최적화된 Repository와 생명주기 훅을 갖춘 JPA/Hibernate 구현체 |
| `infra:persistence:mongo:servlet` | MongoDB 구현체 — `MongoDocument`, `SoftDeletableMongoDocument`, `OptimizedMongoRepository` |
| `infra:persistence:mongo:reactive` | Reactive MongoDB 구현체 — `ProjectionDocument`, `OptimizedReactiveMongoRepository` (버전 가드 upsert + soft delete) |
| `infra:cache:redis` | Redis 기반 캐시 및 분산 락 구현체 |
| `infra:messaging:kafka` | Kafka 프로듀서/컨슈머 구현체 (idempotent producer, `read_committed` 컨슈머) |
| `infra:observability:servlet` / `:reactive` | OMK 기반 OpenTelemetry 계측, 스택별 제공 |
| `infra:client:armeria` | Armeria 기반 HTTP 클라이언트 어댑터 |
| `infra:serializer:jackson` / `:protobuf` | 직렬화 구현체 |
| `infra:security:jwt` | JWT 서명·검증 구현체 |
| `infra:kms:vault` | HashiCorp Vault 기반 키 자료 제공자 |

## 핵심 엔지니어링 포인트

**at-least-once 브로커 위에서의 near-exactly-once 전달**
Kafka는 at-least-once 전달만 보장합니다. Whiteprint는 Outbox/Inbox 구현으로 그 간극을 메웁니다 — 발행 이벤트는 도메인 상태와 한 트랜잭션으로 저장된 뒤 `FOR UPDATE SKIP LOCKED` 폴링으로 안전하게 병렬 소비되고, 수신 이벤트는 eventId 기반 멱등성(CAS)으로 중복 제거됩니다. 여기에 idempotent producer(`enable.idempotence=true`, `retries=MAX_VALUE`)와 `read_committed` 컨슈머를 결합해, 별도의 트랜잭셔널 아웃박스 프레임워크 없이도 exactly-once에 가까운 동작을 구현했습니다.

**Auto Increment·UUID 대신 TSID**
Auto Increment PK는 분산 환경에서 충돌하고, UUID는 그 문제는 해결하지만 B-tree 인덱스를 파편화시키고 저장공간을 늘립니다. TSID는 전역 유일성과 시간 정렬성을 모두 갖춘 `Long` 키를 제공해, 두 문제를 동시에 해결하면서도 인덱스·저장 효율은 그대로 유지합니다.

**다층 방어를 갖춘 키 관리**
JWT 서명은 비대칭 키(RSA) 기반으로 동작합니다 — 서명에 쓰이는 Private Key는 KMS 경계에서 export 자체가 차단되어 HashiCorp Vault 밖으로 절대 나가지 않는 반면, 검증에 쓰이는 Public Key는 외부에 노출돼도 안전하므로 로컬에 캐싱(크기·TTL 제한)해 매 요청마다 Vault를 호출하지 않고도 빠르게 토큰을 검증합니다. 키는 30일 주기로 자동 로테이션되며 무중단 검증을 위한 overlap 구간을 두고, revocation은 토큰 단위·계정 단위 모두 Redis TTL 기반으로 동작해 — 탈취된 토큰 하나, 혹은 계정 전체를 만료를 기다리지 않고 즉시 무효화할 수 있습니다.

**이름뿐이 아닌, 실제로 강제되는 Hexagonal 경계**
`core / adapter / infra` 분리는 네이밍 컨벤션이 아니라 의존성 규칙입니다. `core`는 프레임워크 의존성이 없고, `adapter`가 모든 Spring 연결을 담당하며, 구체적인 기술이 언급되는 곳은 `infra`뿐입니다. 이 원칙이 지켜지기 때문에 플랫폼의 각 부분을 독립적으로 교체할 수 있습니다.

## 기능

**아키텍처 & 도메인**
- core/adapter/infra 경계가 강제되는 Hexagonal Architecture
- Aggregate 기반 DDD 설계, 생명주기 훅 자동 호출(`onCreate`/`onUpdate`/`onDelete`)
- CQRS — Command/Query 서비스 완전 분리
- Apache Kafka 기반 이벤트 기반 아키텍처(EDA)

**메시징**
- at-least-once 전달과 멱등 소비를 보장하는 Outbox/Inbox 패턴

**보안**
- 비대칭 키(RSA) 기반 JWT 발급·검증(Access/Refresh) — Private Key는 Vault에서 export 차단, Public Key는 로컬 캐싱으로 빠른 검증
- 30일 자동 키 로테이션, Redis 기반 토큰·계정 이중 revocation
- 선언적 AOP 권한 어노테이션(`@RequirePermission`, `@ForbidPermission`, `@RequireAnyPermission`, `@RequireAllPermissions`, `@RequireHigherPermissionThan`) — 서블릿·리액티브 양쪽에서 동일한 어노테이션 사용 가능
  - **서블릿**: `@Before` advice — 메서드 본체 실행 전에 권한 거부; `Authorizer` 빈으로 프로그래밍 방식 체크 가능; `SecurityContextSupport.getCurrentClaims()`가 `AccessTokenClaims` 직접 반환
  - **리액티브**: `@Around` advice — 반환된 `Mono`/`Flux`를 Reactor Context 안에서 래핑하여 구독 시점에 권한 체크; `SecurityContextSupport.currentClaims()`가 `Mono<AccessTokenClaims>` 반환; `Authorizer` 빈 없음(Mono 체인 안에서 `SecurityContextSupport` 사용); revocation 체크는 `Schedulers.boundedElastic()`에서 실행되므로 Netty 이벤트 루프는 절대 블로킹되지 않음

**영속성 & 식별자**
- 최적화된 Repository와 자동 생명주기 처리를 갖춘 JPA 영속성 레이어
- MongoDB 영속성 — 서블릿(`MongoDocument` / `OptimizedMongoRepository`)과 리액티브(`ProjectionDocument` / 버전 가드 upsert 적용 `OptimizedReactiveMongoRepository`)
- TSID 기반 PK (전역 유일 + 시간 정렬)

**복원력 & 유연성**
- Redis 기반 분산 락
- web/cache/security/persistence 전반의 서블릿·리액티브 듀얼 스택 어댑터

**설정 없는 자동 구성 (Zero-Boilerplate)**
- **동적 Repository 스캔** — yaml에 패키지만 선언하면(`entity-packages-to-scan` / `repository-packages-to-scan`) 자동 스캔; 사용 서비스에서 `@EnableJpaRepositories`·`@EnableReactiveMongoRepositories` 작성 불필요
- **스케줄링** — publisher·subscriber 어댑터가 `@EnableScheduling`을 자동으로 적용하므로 사용 서비스에서 별도 선언 불필요
- **JPA 가드** — `JpaAutoConfigurationGuard`(`EnvironmentPostProcessor`)가 시작 시점에 `adapter.event.outbox/inbox.infrastructure-implementation`과 `adapter.persistence.infrastructure-implementation`을 검사하여, 어느 모듈도 `jpa`를 사용하지 않으면 Spring Boot의 JPA·JDBC 자동 구성을 자동 제외 — 리액티브 전용 서비스에서 "Failed to configure a DataSource" 오류 발생 없음
- **Outbox/Inbox 듀얼 스택 MongoDB** — reactive WebFlux 서비스에서 HTTP 요청은 `ReactiveMongoTemplate`을 통해 Netty 이벤트 루프 위에서 non-blocking으로 처리; Outbox/Inbox 폴링은 이벤트 루프와 완전히 분리된 `ScheduledThreadPoolExecutor` 위에서 동일한 `ReactiveMongoTemplate`을 `.block()`으로 사용 — 안전하며 MongoDB 클라이언트 이중 설정 불필요

**관측성**
- [Operation Manager Kit(OMK)](https://github.com/Hchanjune/operation-manager-kit) 기반 — OpenTelemetry 계측이 Prometheus, Grafana, Loki, Tempo와 기본 연동

## 기술 스택

Kotlin 2.3.20 · Spring Boot 4.0.6 · Java 21 · Apache Kafka · Redis · PostgreSQL/JPA · HashiCorp Vault · OpenTelemetry

## 시작하기

JitPack 저장소를 추가하고 BOM을 import한 뒤, 필요한 모듈만 골라 사용하면 됩니다 — 각 의존성에 버전을 따로 명시할 필요가 없습니다:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // BOM import — Whiteprint 모든 모듈의 버전이 이 한 줄로 관리됩니다
    implementation(platform("com.github.Hchanjune.Whiteprint:whiteprint-bom:<version>"))

    // 필요한 모듈만 선택 — 버전 명시 불필요
    implementation("com.github.Hchanjune.Whiteprint:adapter-web-servlet")
    implementation("com.github.Hchanjune.Whiteprint:adapter-security-verifier-servlet")
    implementation("com.github.Hchanjune.Whiteprint:adapter-security-provider-servlet")
    implementation("com.github.Hchanjune.Whiteprint:adapter-event-outbox")
    implementation("com.github.Hchanjune.Whiteprint:adapter-event-publisher")
}
```

버전을 올릴 때는 BOM 버전 한 줄만 바꾸면 모든 모듈이 함께 업데이트됩니다.

아티팩트 이름은 Gradle 모듈 경로에서 `platform:` 접두사를 떼고 `:`를 `-`로 바꾼 형태입니다 — 예를 들어 `platform:adapter:web:servlet`은 `adapter-web-servlet`이 됩니다. 전체 목록은 위 모듈 구성 표를 참고하세요. 이 플랫폼 위에서 동작하는 완전한 레퍼런스 구현체(인증 서비스)는 [`sample/`](./sample) 디렉터리에서 확인할 수 있습니다.
