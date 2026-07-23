# Inbox PARTITION_ORDERED 설계 (2026-07-23 확정)

멀티 인스턴스 환경에서 **파티션 키(partition_key) 단위 순서 보장 + 키 간 병렬 처리**를 제공하는
인박스 처리 모드의 설계 문서. 구현 전 확정된 결정과 단계 계획을 기록한다.

## 1. 배경 / 목표

현행 `AbstractEventHandler`는 `@Scheduled(fixedDelay=500)` 폴러가 배치(limit=100)를
**폴 스레드 위에서 직렬로 인라인 처리**한다. 이로 인해:

- 핸들러 처리 시간이 길면(예: 분 단위 AI 호출) 배치가 끝날 때까지 재폴이 정지한다.
- 폴 쿼리에 `ORDER BY`가 없어 **순서는 현재도 보장되지 않는다.**
- claim(`tryAcquire`)이 event 단위 CAS라, 멀티 인스턴스에서는 **같은 키의 서로 다른
  이벤트가 동시에 처리될 수 있다** (오늘도 per-key 순서 무보장).

목표: 카프카가 파티션 소유권으로 제공하는 "같은 키 직렬·순서 / 다른 키 병렬"을,
인박스(경쟁 폴링) 모델 위에서 **DB를 조율자로 삼아** 재현한다.
(SQS FIFO message group / Service Bus session 과 동일한 패턴)

## 2. 확정 결정 (Decision Log)

| # | 결정 | 근거 |
|---|---|---|
| D1 | 인메모리 레인 폐기, **DB key 게이트** 채택 | 인메모리 구조는 인스턴스 경계를 못 넘음. k8s 오토스케일 전제. DB 게이트는 non-sticky(키가 인스턴스에 고정되지 않음 → 리밸런싱 불요, 스케일아웃 시 새 인스턴스가 claim 경쟁에 합류만 하면 됨) |
| D2 | 락 단위 = **`partition_key` 단독** (`(eventType, key)` 아님) | 키는 서러게이트(TSID)라 단독으로 유일. 도메인 이벤트가 인과체인(다음 타입 이벤트는 이전 타입 처리 후 생성)이라 같은 키의 크로스타입 co-occur가 없음 → 동시성 손해 0. 가정이 깨져도 실패 방향이 "레이스 버그"가 아닌 "직렬화(느려짐)"인 fail-safe |
| D3 | claim 원자화 = **키 단위 상호배제 락** + `PROCESSING` 상태가 처리 기간 동안 게이트 유지 | `NOT EXISTS` 검사만으로는 같은 키의 서로 다른 두 이벤트를 두 인스턴스가 동시에 claim하는 레이스가 남음(각자 다른 row라 row lock으로 안 막힘). 락은 claim 순간만 잡고, 이후는 영속화된 상태가 게이트 역할 |
| D4 | **실패 정책: FAILED(재시도 대기) → 총 3회 실행 소진 시 DEAD(종결)** — 게이트는 `PROCESSING`+`FAILED`+`DEAD` 모두 검사(키 블로킹) | 실패 이벤트를 건너뛰면 순서 위반 → head-of-line 블로킹 감수. FAILED 는 backoff(30s→60s) 후 자동 재시도(조용함, WARN). DEAD 는 종결 — 알림([DeadEventNotifier] 전역 빈 + `onEventDead` 핸들러 훅)을 던지고 수동 복구(status='RECEIVED', attempt_count=0) 전까지 키 정지. 순서 포기(스킵)는 자동으로 일어나지 않는다. 멱등성은 순서의 대체재가 아니라 재시도(at-least-once)의 보완재 |
| D5 | **frontier 조회 필수**: 키당 최선두 1건만, 최고령 순 | 단순 `ORDER BY … LIMIT` 은 한 키의 백로그가 조회 윈도우를 침수시켜 다른 키를 기아 상태로 만듦. frontier 방식이면 윈도우가 "이벤트 N개"가 아니라 "서로 다른 키 N개"를 커버 |
| D6 | **claim ≤ 빈 워커 슬롯**, 조회 LIMIT ≈ 빈 슬롯 × 2~3 | over-claim 금지: claim만 하고 큐에 쌓으면 타 인스턴스에 안 보여 밸런싱 파괴 + 인스턴스 사망 시 stale 폭탄. 여유분(×2~3)은 멀티 인스턴스 claim 경쟁 패배 흡수용 |
| D7 | 처리 모드 3종 **opt-in**: `SERIAL`(기본=현행) / `PARALLEL` / `PARTITION_ORDERED` | 인박스는 범용 플랫폼 컴포넌트 — 게이트 비용을 전 핸들러에 강제하지 않음. per-key 게이트 비용은 핸들러 지연에 비례(ms 핸들러는 체감 없음, 분 단위 핸들러가 최악 케이스). 기본 SERIAL로 기존 서비스 하위호환 |
| D8 | rate limit(외부 프로바이더별 동시 호출 상한)은 **본 설계 스코프 밖** | 병렬도 노브는 워커풀 크기 × 인스턴스 수. 프로바이더별 상한은 후속 레이어로 분리 |
| D9 | **JPA(Postgres)와 Mongo 모두 구현** | 플랫폼이 두 스토어를 지원하므로 모드도 양쪽 제공. 원자화 프리미티브만 스토어별로 다름(§5) |

## 3. 보장 명세 (Invariants)

PARTITION_ORDERED 모드에서:

1. **키당 전역 1개**: 어떤 partition_key든 전체 fleet에서 동시에 최대 1개의 이벤트만
   PROCESSING. (키가 인스턴스를 옮겨 다녀도 겹침 없음 — non-sticky)
2. **키 내 시간순**: 같은 키의 이벤트는 `event_id`(TSID=시간정렬) 오름차순으로 처리.
3. **키 간 병렬**: 서로 다른 키는 제약 없이 병렬.
   실효 병렬도 = min(활성 키 수, Σ워커풀, 커넥션 풀).
4. **기아 없음**: frontier 조회(D5) + 최고령 우선(FIFO)으로, 대기 키는 유한 시간 내 처리.
   한 키는 슬롯을 1개만 점유 가능하므로 독식 불가.
5. **실패 시 키 블로킹 + 자동 재시도**: 예외 → FAILED(키 블로킹, backoff 30s→60s 후
   자동 RECEIVED 복귀) → 총 `maxAttempts`(기본 3)회 실행 소진 시 DEAD(키 블로킹 유지,
   알림 발화, 수동 복구 전까지 정지). FAILED/DEAD 모두 게이트에 포함되어 순서가
   자동으로 스킵되는 일은 없다.
6. **장애 복구(하트비트)**: 처리 중인 이벤트는 30초마다 `last_attempted_at` 하트비트를
   갱신한다. stale 스캐너는 "하트비트가 `stale-timeout-millis`(기본 120초) 이상 끊긴
   PROCESSING"만 사망으로 판정해 RECEIVED 복귀 — 처리 시간이 아무리 길어도 살아있는
   처리를 회수하지 않는다(산 놈/죽은 놈을 시간 임계 추정이 아닌 생존 신호로 구분).
   하트비트 펌프는 Spring 스케줄러 풀과 분리된 전용 데몬 스레드에서 돈다.
   재처리 안전성은 핸들러 멱등성이 담당(at-least-once).

## 4. 처리 흐름 (PARTITION_ORDERED)

```
pollAndProcess (fixedDelay=500, 폴 스레드 — 절대 블로킹하지 않음):
  freeSlots = 워커풀 빈 슬롯 수
  if freeSlots == 0: return
  frontiers = store.findClaimableFrontiers(eventType, limit = freeSlots * 3)
              // 키당 최선두 1건, PROCESSING/FAILED 키 제외, event_id 오름차순
  claimed = 0
  for record in frontiers:
    if claimed >= freeSlots: break
    if !store.tryAcquireOrdered(record.eventId, record.partitionKey): continue
              // 키 락 획득 실패(타 인스턴스 경쟁) 또는 게이트 불통과 → 스킵
    claimed++
    workerPool.execute { processOne(record) }   // 완료 시 슬롯 반환
```

- claim은 폴 스레드에서(중복 방지 유지), 처리는 워커풀에서.
- `processOne` 은 기존과 동일: 성공 → COMPLETED, 예외 → FAILED(+ 알림 훅 `onEventFailed`).

## 5. 스토어별 claim 원자화

두 스토어 모두 동일 계약: **"키 락을 잡은 주체만이, 그 키에 PROCESSING/FAILED가
없음을 확인하고, 최선두 RECEIVED 1건을 PROCESSING으로 마킹한다."**
키 락은 claim 순간만 유지하고(짧음), 처리 기간의 배제는 PROCESSING 상태가 담당.

### 5.1 JPA (Postgres)

- 키 락: `pg_try_advisory_xact_lock(partition_key)` — 트랜잭션 종료 시 자동 해제.
  획득 실패 = 타 인스턴스가 이 키를 claim 중 → 스킵.
- 게이트: `NOT EXISTS (SELECT 1 FROM event_inbox WHERE partition_key = :pk
  AND status IN ('PROCESSING','FAILED'))`
- frontier: `SELECT DISTINCT ON (partition_key) …` 서브쿼리 후 외부에서
  `ORDER BY event_id LIMIT :n` (Postgres 전용 네이티브 쿼리).
- 인덱스: `(partition_key, status)`, 폴 보조로 `(event_type, status)` 확인.

### 5.2 Mongo

advisory lock이 없으므로 **claim-lock 컬렉션**으로 키 상호배제를 구현:

- `event_inbox_key_locks` 컬렉션: `{ _id: partitionKey, lockedAt }`.
  - 키 락 획득 = `insert` 시도. `DuplicateKeyException` = 타 인스턴스가 claim 중 → 스킵.
  - claim 완료(성공/스킵 불문) 즉시 `delete` — 락 문서는 밀리초 수명.
  - 크래시로 남은 락 문서는 `lockedAt` **TTL 인덱스(수십 초)** 로 자동 청소.
- 게이트: 같은 키에 `status ∈ {PROCESSING, FAILED}` 문서 존재 여부 조회.
- 마킹: `findOneAndUpdate({eventId, status: RECEIVED} → PROCESSING)` (단일 문서 원자).
- frontier: aggregation — `match(eventType, RECEIVED)` → `sort(eventId)` →
  `group(partitionKey, first)` 후 블로킹 키(PROCESSING/FAILED 보유) 제외 →
  `sort(eventId)` → `limit(n)`.
- 인덱스: `(eventType, status, partitionKey)`, `(partitionKey, status)`,
  락 컬렉션 `lockedAt` TTL.

참고: Mongo 멀티도큐먼트 트랜잭션은 스냅샷 읽기 특성상 "서로 다른 문서를 쓰는 두 claim"의
충돌을 감지하지 못하므로 게이트 레이스를 못 닫는다 — 락 컬렉션 방식이 필수.

## 6. 플랫폼 API (`AbstractEventHandler`)

```kotlin
enum class ProcessingMode { SERIAL, PARALLEL, PARTITION_ORDERED }

abstract class AbstractEventHandler<E: Event> {
    /** 기본 SERIAL = 현행 동작 그대로 (하위호환). 핸들러가 override로 opt-in. */
    protected open val processingMode: ProcessingMode = ProcessingMode.SERIAL

    /** PARALLEL/PARTITION_ORDERED에서 인스턴스당 동시 처리 상한. 기본 10. */
    protected open val workerPoolSize: Int = 10

    /** FAILED 발생 훅 — PARTITION_ORDERED에선 키 블로킹을 의미하므로 알림 연동 지점. */
    protected open fun onEventFailed(record: EventInbox, exception: Exception) {}
}
```

- 워커풀: lazy 생성, `@PreDestroy` shutdown. 슬롯 회계는 인스턴스 로컬
  카운터(Semaphore)로 — D8의 rate limit과는 무관한 구현 디테일.
- `SERIAL` 경로는 코드 변경 없음(기존 서비스 무영향).
- `PARALLEL` = 기존 event 단위 CAS claim + 워커풀 dispatch (순서 무보장, 멱등 핸들러용).

### 설정 기본값

| 항목 | 기본 | 비고 |
|---|---|---|
| `processingMode` | SERIAL | 하위호환 |
| `workerPoolSize` | 10 | 인스턴스·핸들러당. **Hikari 기본(10)과 동수이므로 PARTITION_ORDERED 적용 서비스는 커넥션 풀 상향(권장: workerPoolSize+여유 ≥ 15~20) 필요** |
| frontier LIMIT | `freeSlots × 3` | claim 경쟁 패배 흡수 |
| `maxAttempts` | 3 | 총 실행 횟수(최초 1 + 재시도 2). 소진 시 DEAD. attempt_count 는 claim 마다 증가하므로 크래시 재claim 도 카운트됨 |
| `retryBackoffBaseMillis` | 30,000 | n회차 실패 후 대기 = base × 2^(n-1) → 30s, 60s |
| 하트비트 주기 | 30초 (고정) | 처리 중 이벤트의 last_attempted_at 갱신. 전용 데몬 스레드 |
| `adapter.event.subscriber.stale-timeout-millis` | 120,000 | 하트비트 두절 임계(주기의 3~4배). 구 claim-timeout-millis 를 rename — 처리시간 상한이 아니라 생존 판정 기준 |
| DEAD 알림 | — | 전역: `DeadEventNotifier` 빈 1개 등록(모든 핸들러 공통) / 개별: `onEventDead` override. 노티파이어 → 훅 순, 예외는 삼켜짐 |

## 7. 구현 단계

- **Phase 1 — `core:messaging`**: `ProcessingMode` enum,
  `EventInboxStore`에 `findClaimableFrontiers(eventType, limit)` /
  `tryAcquireOrdered(eventId, partitionKey)` 추가.
- **Phase 2 — `adapter:event:inbox` (JPA)**: Postgres 네이티브 쿼리(frontier,
  advisory-lock claim), 인덱스 DDL(플랫폼 샘플 SQL + 각 서비스 IObox.sql).
- **Phase 3 — `adapter:event:inbox` (Mongo)**: claim-lock 컬렉션(+TTL 인덱스),
  aggregation frontier, `MongoEventInboxStore` 구현.
- **Phase 4 — `adapter:event:subscriber`**: `AbstractEventHandler` 모드 분기,
  워커풀 lifecycle, 슬롯 회계, `onEventFailed` 훅, FAILED ERROR 로그.
  `recoverStaleProcessing` 유지(주석으로 키 블로킹 연관성 명시).
- **Phase 5 — 검증**: 동시성 실측 테스트 —
  (a) 2스레드(=2인스턴스 모사) 동시 claim 경쟁에서 같은 키 동시 처리 0건·순서 유지,
  (b) FAILED 키 블로킹 + 타 키 정상,
  (c) 한 키 백로그 대량 상황에서 타 키 기아 없음(frontier),
  (d) stale 리셋 후 순서 재개. JPA/Mongo 각각.
- **Phase 6 — 배포/적용**: 플랫폼 버전 bump(0.7.0) 및 배포(사용자),
  소비 서비스에서 필요 핸들러만 PARTITION_ORDERED opt-in + 인덱스 적용 +
  커넥션 풀 상향, 멀티 인스턴스(2개) 기동 실측.

## 8. 스코프 밖 (후속 과제)

- DEAD 알림의 실제 채널 연동(슬랙/페이저 등) — 포트(`DeadEventNotifier`)까지는 구현됨,
  각 서비스가 빈으로 채널을 꽂으면 됨.
- 외부 프로바이더별 동시 호출 상한(rate limit) 레이어 (D8).
- 기존 인박스 조회/restore API를 "블로킹 키 복구" 운영 도구로 정비.
- 초고빈도(초당 수만+) 스트림은 DB 인박스의 적용 경계 밖 — 해당 워크로드는
  브로커 네이티브 처리로 설계할 것.
