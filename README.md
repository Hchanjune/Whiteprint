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
| `core:cache` | Cache and distributed-lock contracts |
| `core:messaging` | Event, publisher/subscriber, and Outbox/Inbox contracts |
| `core:kms` | Key material, caching, and rotation-policy contracts |
| `core:security` | JWT/token verification, claims, and revocation contracts |

**`platform/adapter`** — Spring Boot wiring (servlet & reactive stacks)

| Module | Purpose |
| --- | --- |
| `adapter:serializer` | Serialization port wiring |
| `adapter:persistence:servlet` / `:reactive` | JPA / R2DBC persistence wiring |
| `adapter:event:outbox` / `:inbox` | Outbox/Inbox polling & idempotency wiring |
| `adapter:event:publisher` / `:subscriber` | Event publishing & subscription wiring |
| `adapter:cache:servlet` / `:reactive` | Cache wiring per stack |
| `adapter:lock:distributed` | Distributed-lock wiring |
| `adapter:security:provider:servlet` / `:reactive` | Token issuance (login/refresh) wiring |
| `adapter:security:verifier:servlet` / `:reactive` | Token verification & filter-chain wiring |
| `adapter:web:servlet` / `:reactive` | Web-layer conventions — exception handling, response wrapping |

**`platform/infra`** — concrete technology implementations

| Module | Purpose |
| --- | --- |
| `infra:persistence:jpa` | JPA/Hibernate implementation with optimized repositories & lifecycle hooks |
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

**Persistence & Identity**
- JPA persistence layer with optimized repositories and automatic lifecycle handling
- TSID-based primary keys (globally unique, time-sortable)

**Resilience & Flexibility**
- Redis-backed distributed locking
- Dual-stack adapters — servlet and reactive — across web, cache, security, and persistence

**Observability**
- Built on [Operation Manager Kit (OMK)](https://github.com/Hchanjune/operation-manager-kit) — OpenTelemetry instrumentation wired to Prometheus, Grafana, Loki, and Tempo

## Tech Stack

Kotlin 2.3.20 · Spring Boot 4.0.6 · Java 21 · Apache Kafka · Redis · PostgreSQL/JPA · HashiCorp Vault · OpenTelemetry

## Getting Started

Add Whiteprint to your Gradle build via JitPack — that's it:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Hchanjune.Whiteprint:core-kernel:<version>")
    implementation("com.github.Hchanjune.Whiteprint:adapter-web-servlet:<version>")
    implementation("com.github.Hchanjune.Whiteprint:infra-messaging-kafka:<version>")
}
```

Artifact names are the module's Gradle path with the `platform:` prefix dropped and `:` replaced by `-` — e.g. `platform:adapter:web:servlet` becomes `adapter-web-servlet` (see the Module Reference tables above for the full list). Pick whichever `platform:*` modules your service needs — there's no single monolithic dependency to pull in. A complete reference implementation (an auth service built entirely on this platform) lives under [`sample/`](./sample).
