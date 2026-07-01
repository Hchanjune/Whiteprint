# Whiteprint

**A platform library for event-driven, CQRS-based microservices — built so teams stop reinventing the same distributed-systems plumbing in every service.**

[한국어 README](./README.ko.md)

## Why Whiteprint

Every distributed system ends up solving the same handful of hard problems — reliable messaging, persistence, authentication, caching, observability — usually slightly differently, and scattered across services. Whiteprint consolidates that recurring plumbing into a single, consistent **platform layer**, so the teams building on top of it can spend their time on business logic instead of re-solving — and re-fragmenting — the same cross-cutting concerns over and over.

## Architecture

Whiteprint is built on **Hexagonal Architecture**, with a strict three-layer separation that's enforced at the module level — not just by convention:

```
┌────────────────────────────────────┐
│            Your Service             │   business logic only
├────────────────────────────────────┤
│           platform / core           │   framework-agnostic contracts (ports)
├────────────────────────────────────┤
│          platform / adapter         │   Spring Boot wiring
├────────────────────────────────────┤
│           platform / infra          │   Kafka · Redis · JPA · Vault · …
└────────────────────────────────────┘
```

- **`core`** — Pure domain abstractions and contracts. Zero framework dependencies — these modules define *what* the platform provides, not *how*.
- **`adapter`** — Connects `core` contracts to Spring Boot (auto-configuration, properties binding, bean wiring) without leaking framework details back into the domain.
- **`infra`** — The only layer where a specific technology is named. Concrete implementations of `core` contracts against Kafka, Redis, JPA, Vault, and friends.

Because services depend on `core` abstractions — never on Kafka or Redis directly — swapping an infrastructure choice means writing a new `infra` module against an existing contract, not rewriting business logic.

## Module Reference

**`platform/core`** — framework-agnostic contracts

| Module | Purpose |
| --- | --- |
| `core:kernel` | Shared base contracts — `Identifiable`, `Auditable`, `LifeCycle`, policy/exception conventions |
| `core:domain` | Aggregate-based domain modeling primitives |
| `core:projection` | Read-side projection contracts — `Projection` (id, version, soft-delete, audit fields) and marker interfaces `Query`, `QueryParams`, `ViewModel` |
| `core:cache` | Cache contracts |
| `core:lock` | Distributed-lock contracts — `LockKey`, `LockHandle`, `DistributedLockOperations`, `@DistributedLock`/`@DistributedLockKey` |
| `core:messaging` | Event, publisher/subscriber, and Outbox/Inbox contracts |
| `core:kms` | Key material, caching, and rotation-policy contracts |
| `core:security` | JWT/token verification, claims, revocation contracts, and declarative permission annotations (`@RequirePermission`, `@ForbidPermission`, `@RequireAnyPermission`, `@RequireAllPermissions`, `@RequireHigherPermissionThan`) |

**`platform/adapter`** — Spring Boot wiring (servlet & reactive stacks)

| Module | Purpose |
| --- | --- |
| `adapter:serializer` | Serialization port wiring |
| `adapter:persistence:servlet` | JPA persistence wiring — `JpaRepositoryRegistrar` dynamically scans repositories from `adapter.persistence.jpa.options.entity-packages-to-scan`; `@EnableJpaRepositories` not needed in consuming services |
| `adapter:persistence:reactive` | Reactive MongoDB persistence wiring — `ReactiveMongoRepositoryRegistrar` dynamically scans from `adapter.persistence-reactive.reactive-mongo.options.repository-packages-to-scan`; `@EnableReactiveMongoRepositories` not needed |
| `adapter:event:outbox` / `:inbox` | Outbox/Inbox polling & idempotency wiring — JPA and MongoDB backends switchable via `adapter.event.outbox/inbox.infrastructure-implementation`; MongoDB backend auto-selects implementation by web stack — servlet uses `MongoTemplate` (sync), reactive uses `ReactiveMongoTemplate` with `.block()` on a `ScheduledThreadPoolExecutor` thread (safe, entirely off the Netty event loop); includes `JpaAutoConfigurationGuard` that automatically excludes Spring Boot JPA/JDBC auto-configuration in non-JPA services |
| `adapter:event:publisher` | Event publishing wiring — `@EnableScheduling` auto-applied; not needed in consuming services |
| `adapter:event:subscriber` | Event subscription wiring — `@EnableScheduling` auto-applied; not needed in consuming services |
| `adapter:cache:servlet` / `:reactive` | Cache wiring per stack |
| `adapter:lock:distributed:servlet` | Distributed-lock AOP wiring (servlet stack) |
| `adapter:security:provider:servlet` / `:reactive` | Token issuance (login/refresh) wiring |
| `adapter:security:verifier:servlet` | Token verification & filter-chain wiring (servlet) — `@Before` AOP, `SecurityContextSupport.getCurrentClaims()`, `Authorizer` bean |
| `adapter:security:verifier:reactive` | Token verification & filter-chain wiring (reactive) — `@Around` AOP wrapping `Mono`/`Flux`, `SecurityContextSupport.currentClaims(): Mono<AccessTokenClaims>` |
| `adapter:web:servlet` | Web-layer conventions — exception handling, response wrapping; `ResponseEntityGenerator` reads traceId via `Operations.context.traceId` (thread-local) |
| `adapter:web:reactive` | WebFlux web-layer conventions — `ResponseEntityGenerator.generateInstantData()` returns `Mono<ResponseEntity<ApiResponse<T>>>`, reading traceId from Reactor Context via `ReactiveOperations`; `PlatformExceptionHandler` maps WebFlux-specific exceptions (`WebExchangeBindException`, `ServerWebInputException`, `ResponseStatusException`, etc.) |

**`platform/infra`** — concrete technology implementations

| Module | Purpose |
| --- | --- |
| `infra:persistence:jpa` | JPA/Hibernate implementation with optimized repositories & lifecycle hooks |
| `infra:persistence:mongo:servlet` | MongoDB implementation — `MongoDocument`, `SoftDeletableMongoDocument`, `OptimizedMongoRepository` |
| `infra:persistence:mongo:reactive` | Reactive MongoDB implementation — `ProjectionDocument` (MongoDB document base with `@Id`, field mapping), `ProjectionRepository<T>` (coroutine-first interface extending `CoroutineCrudRepository<T, String>` with `suspend fun upsert`), `ProjectionRepositorySupport<T>` (reactive backing class registered as `repositoryBaseClass` — standard CRUD auto-wrapped to suspend by Spring Data, custom methods matched by signature) |
| `infra:cache:redis` | Redis-backed cache & distributed-lock implementation |
| `infra:messaging:kafka` | Kafka producer/consumer implementation (idempotent producer, `read_committed` consumer) |
| `infra:observability:servlet` / `:reactive` | OpenTelemetry instrumentation via OMK, per stack |
| `infra:client:armeria` | Armeria-based HTTP client adapter |
| `infra:serializer:jackson` / `:protobuf` | Serialization implementations |
| `infra:security:jwt` | JWT signing & verification implementation |
| `infra:kms:vault` | HashiCorp Vault-backed key material provider |

## Engineering Highlights

**Near-exactly-once delivery on an at-least-once broker**
Kafka only guarantees at-least-once delivery. Whiteprint closes that gap with an Outbox/Inbox implementation: outbound events are persisted transactionally with domain state and drained via `FOR UPDATE SKIP LOCKED` polling (safe, lock-free concurrent consumption), while inbound events are deduplicated through event-id based idempotency (CAS). Paired with an idempotent producer (`enable.idempotence=true`, `retries=MAX_VALUE`) and a `read_committed` consumer, the result behaves close to exactly-once — without bolting on a transactional-outbox framework.

**TSID over auto-increment or UUID**
Auto-increment primary keys collide across distributed writers; UUIDs fix that but fragment B-tree indexes and bloat storage. TSID gives globally-unique, time-sortable `Long` keys — solving both problems simultaneously while staying index- and storage-friendly.

**Defense-in-depth key management**
JWT signing is built on asymmetric (RSA) cryptography — the private signing key never leaves HashiCorp Vault (export is disabled at the KMS boundary), while the public verification key is safe to distribute and gets cached locally (size- and TTL-bounded) so verification doesn't round-trip to Vault on every request. Keys rotate automatically on a 30-day cycle with overlap windows for zero-downtime verification, and revocation works at both the token and account level via Redis TTL — so a single compromised token, or an entire compromised account, can be invalidated immediately rather than waiting out an expiry.

**Hexagonal boundaries that are actually enforced**
The `core / adapter / infra` split isn't a naming convention — it's a dependency rule. `core` has no framework dependencies, `adapter` owns all the Spring wiring, and `infra` is the only place a concrete technology gets referenced. That discipline is what makes the platform's pieces independently swappable.

## Features

**Architecture & Domain**
- Hexagonal Architecture with enforced core/adapter/infra boundaries
- DDD-style Aggregates with automatic lifecycle hooks (`onCreate` / `onUpdate` / `onDelete`)
- CQRS — full command/query service separation
- Event-Driven Architecture on Apache Kafka

**Messaging**
- Outbox / Inbox pattern for at-least-once delivery and idempotent consumption

**Security**
- JWT issuance & verification (access/refresh) on asymmetric (RSA) keys — private keys stay in Vault (export disabled), public keys cached locally for fast verification
- Automatic 30-day key rotation and dual-level (token + account) revocation via Redis
- Declarative AOP permission annotations (`@RequirePermission`, `@ForbidPermission`, `@RequireAnyPermission`, `@RequireAllPermissions`, `@RequireHigherPermissionThan`) — same annotations work on both stacks
  - **Servlet**: `@Before` advice throws before the method body runs; `Authorizer` bean available for programmatic checks; `SecurityContextSupport.getCurrentClaims()` returns `AccessTokenClaims` directly
  - **Reactive**: `@Around` advice wraps the returned `Mono`/`Flux` inside the Reactor context — permission is denied when the publisher is subscribed, not when the method is called; `SecurityContextSupport.currentClaims()` returns `Mono<AccessTokenClaims>`; no `Authorizer` bean (use `SecurityContextSupport` in the Mono chain instead); revocation check runs on `Schedulers.boundedElastic()` so the Netty event loop is never blocked

**Persistence & Identity**
- JPA persistence layer with optimized repositories and automatic lifecycle handling
- MongoDB persistence — servlet (`MongoDocument` / `OptimizedMongoRepository`) and reactive (`ProjectionDocument` / `ProjectionRepository<T>` coroutine-first with version-guard upsert & soft delete — `suspend fun` out of the box, no `.awaitX()` boilerplate)
- TSID-based primary keys (globally unique, time-sortable)

**Resilience & Flexibility**
- Redis-backed distributed locking
- Dual-stack adapters — servlet and reactive — across web, cache, security, and persistence

**Zero-Boilerplate Auto-Configuration**
- **Dynamic repository scanning** — declare packages in yaml (`entity-packages-to-scan` / `repository-packages-to-scan`); `@EnableJpaRepositories` and `@EnableReactiveMongoRepositories` are never needed in consuming services. **Repository registration has a single owner per stack** — `JpaRepositoryRegistrar` (owned by `adapter:persistence:servlet`) scans the entire `org.whiteprint` package tree in one pass, covering all JPA repositories across every platform module and every consuming service; `MongoConfiguration` (same module) does the same for servlet-stack MongoDB repositories; `ReactiveMongoConfiguration` (`adapter:persistence:reactive`) covers reactive MongoDB repositories. Adding `@EnableJpaRepositories` or `@EnableMongoRepositories` anywhere else — a platform module's `@Configuration`, a consuming service's `@SpringBootApplication`, or an individual adapter config such as `JpaEventOutboxConfiguration` — causes Spring to register the same beans a second time and fail at startup with a duplicate-bean error
- **Scheduling** — `@EnableScheduling` is applied automatically by the publisher and subscriber adapters; consuming services need not declare it
- **JPA guard** — `JpaAutoConfigurationGuard` (`EnvironmentPostProcessor`) inspects `adapter.event.outbox/inbox.infrastructure-implementation` and `adapter.persistence.infrastructure-implementation` at startup; if none resolves to `jpa`, Spring Boot's JPA and JDBC auto-configurations are excluded automatically — reactive-only services never fail with "Failed to configure a DataSource"
- **Sync MongoDB guard** — `ReactiveMongoAutoConfigurationGuard` (`EnvironmentPostProcessor`) detects `adapter.persistence-reactive.infrastructure-implementation: reactive_mongo` and automatically excludes Spring Boot's sync MongoDB auto-configuration (`MongoAutoConfiguration`) — prevents an unwanted sync `MongoClient` from being created at `localhost:27017` when the sync driver is transitively on the classpath; `ReactiveMongoHealthIndicator` handles health checks instead
- **Outbox/Inbox dual-stack MongoDB** — servlet services use `MongoTemplate` (sync driver); reactive services use `ReactiveMongoTemplate` — HTTP requests run non-blocking on the Netty event loop while Outbox/Inbox polling runs on a `ScheduledThreadPoolExecutor` (entirely separate from the event loop), so `.block()` is safe there; the correct implementation is selected automatically via `@ConditionalOnWebApplication`

**Observability**
- Built on [Operation Manager Kit (OMK)](https://github.com/Hchanjune/operation-manager-kit) — OpenTelemetry instrumentation wired to Prometheus, Grafana, Loki, and Tempo

## Tech Stack

Kotlin 2.3.20 · Spring Boot 4.0.6 · Java 21 · Apache Kafka · Redis · PostgreSQL/JPA · HashiCorp Vault · OpenTelemetry

## Getting Started

Add the JitPack repository and import the BOM, then pick whichever modules your service needs — no per-dependency version required:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Import the BOM — all Whiteprint module versions are managed from this one line
    implementation(platform("com.github.Hchanjune.Whiteprint:whiteprint-bom:<version>"))

    // Pick the modules your service needs — no version needed
    implementation("com.github.Hchanjune.Whiteprint:adapter-web-servlet")
    implementation("com.github.Hchanjune.Whiteprint:adapter-security-verifier-servlet")
    implementation("com.github.Hchanjune.Whiteprint:adapter-security-provider-servlet")
    implementation("com.github.Hchanjune.Whiteprint:adapter-event-outbox")
    implementation("com.github.Hchanjune.Whiteprint:adapter-event-publisher")
}
```

To upgrade, change only the BOM version — all modules update together.

Artifact names follow a simple rule: drop the `platform:` prefix from the Gradle module path and replace `:` with `-` — e.g. `platform:adapter:web:servlet` becomes `adapter-web-servlet`. See the Module Reference tables above for the full list. A complete reference implementation (an auth service built entirely on this platform) lives under [`sample/`](./sample).
