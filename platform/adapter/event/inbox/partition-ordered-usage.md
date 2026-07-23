# Inbox 처리 모드 사용 가이드 (PARTITION_ORDERED / 재시도 / 하트비트)

핸들러 관점의 실용 가이드. 설계 배경·근거는 [partition-ordered-design.md](./partition-ordered-design.md) 참고.

## 1. 처리 모드 선택

| 모드 | 보장 | 언제 쓰나 |
|---|---|---|
| `SERIAL` (기본) | 인스턴스 내 직렬. 순서 무보장 | 저볼륨, 기존 동작 유지 |
| `PARALLEL` | 경쟁 소비 병렬. 순서 무보장 | 순서 무관 + 멱등 핸들러 (카운터, 알림 등) |
| `PARTITION_ORDERED` | **같은 partition_key 는 전 인스턴스 통틀어 1건씩 시간순, 다른 키는 병렬** | 순서가 중요한 핸들러 (프로젝션, 상태머신, 긴 처리) |

## 2. 빠른 시작 (PARTITION_ORDERED)

### 핸들러 opt-in

```kotlin
@Component
class MyEventHandler(
    private val useCase: MyUseCase,
): AbstractEventHandler<MyEvent>() {
    override val eventType: String = "my.event.type"
    override val eventClass: KClass<MyEvent> = MyEvent::class
    override val processingMode: ProcessingMode = ProcessingMode.PARTITION_ORDERED
    // override val workerPoolSize: Int = 10          // 필요 시 조정
    // override val maxAttempts: Int = 3              // 필요 시 조정
    // override val retryBackoffBaseMillis: Long = 30_000

    @ManagedEventHandler
    override fun handle(event: MyEvent) { useCase.handle(event.toCommand()) }
}
```

### 적용 전 체크리스트

- [ ] **이벤트가 `PartitionedEvent` 를 구현하고 의미 있는 키를 반환하는가?**
  ```kotlin
  data class MyEvent(...): AbstractTraceableEvent(), IntegrationEvent, PartitionedEvent {
      override fun partitionKey(): Long = aggregateId   // 순서 보장의 단위
  }
  ```
  ⚠️ **미구현 시 partitionKey = 0L 로 폴백** → 그 타입의 **모든 이벤트가 한 키로 묶여
  전역 직렬화**된다(병렬성 소멸). PARTITION_ORDERED 를 켜기 전 반드시 확인.
- [ ] **인덱스 DDL** (JPA/Postgres — 각 서비스 IObox.sql 에 포함, 기존 DB엔 수동 실행):
  ```sql
  CREATE INDEX idx_event_inbox_partition_key_status ON event_inbox(partition_key, status);
  CREATE INDEX idx_event_inbox_event_type_status ON event_inbox(event_type, status);
  ```
  (Mongo 는 claim-lock 컬렉션·TTL 인덱스를 스토어가 런타임에 자동 보장 — DDL 불필요)
- [ ] **커넥션 풀 상향** — 처리 건마다 커넥션을 점유하므로 Hikari `maximum-pool-size` ≥
  `workerPoolSize + 여유` (기본 10+10 → **20 권장**).
- [ ] 멀티 인스턴스 기동 시: 같은 Kafka 컨슈머 그룹, `server.port` 만 분리.
  리밸런싱·인스턴스 고정 없음 — 새 인스턴스는 그냥 claim 경쟁에 합류한다.

## 3. 설정 레퍼런스

### 코드 (핸들러별 override)

| 프로퍼티 | 기본 | 의미 |
|---|---|---|
| `processingMode` | `SERIAL` | 처리 모드 |
| `workerPoolSize` | 10 | 인스턴스당 동시 처리(in-flight) 상한. 가상 스레드 + 슬롯 세마포어 |
| `maxAttempts` | 3 | **총 실행 횟수**(최초 1 + 재시도 2). 소진 시 DEAD. 크래시 후 재claim 도 카운트됨 |
| `retryBackoffBaseMillis` | 30,000 | n회차 실패 후 대기 = base × 2^(n-1) → 30s, 60s |

### yaml (서비스 전역)

```yaml
adapter:
  event:
    subscriber:
      # 하트비트 두절 임계 — "이 시간 이상 하트비트 없는 PROCESSING = 처리 주체 사망" 판정.
      # 처리 시간과 무관(살아있는 처리는 30초마다 갱신). 하트비트 주기의 3~4배 권장.
      stale-timeout-millis: 120000
```

## 4. 실패 · 재시도 · DEAD 라이프사이클

```
RECEIVED → PROCESSING ── 성공 → COMPLETED
               ↓ 예외
             FAILED   (키 블로킹 · WARN 로그 · 알림 없음)
               ↓ backoff 경과 → 재시도 스케줄러가 RECEIVED 복귀
             … 총 maxAttempts 회 실행까지 반복 …
               ↓ 마지막 실행도 실패
             DEAD     (키 블로킹 유지 · ERROR 로그 · 🔔 알림)
               ↓ 수동 복구만
             status='RECEIVED', attempt_count=0
```

| 실행 회차 | 실패 시 | 다음 재시도까지 |
|---|---|---|
| 1 | FAILED | 30초 |
| 2 | FAILED | 60초 |
| 3 (=maxAttempts) | **DEAD** | — (수동 복구) |

- PARTITION_ORDERED 에서 FAILED/DEAD 는 **해당 키의 후속 이벤트를 전부 정지**시킨다
  (순서 보장 — 실패 이벤트를 건너뛰지 않음). 다른 키는 영향 없음.
- 재시도 스케줄러는 30초 주기로 돌므로 실제 재시도 시점은 backoff + 최대 30초.

## 5. DEAD 알림 연동

**서비스 전역 (권장)** — 빈 하나 등록하면 그 서비스의 **모든 핸들러**에 적용:

```kotlin
@Configuration
class InboxAlertConfiguration {
    @Bean
    fun deadEventNotifier(slackClient: SlackClient): DeadEventNotifier =
        DeadEventNotifier { record, exception ->
            slackClient.send(
                "🔥 Inbox DEAD — type=${record.eventType}, eventId=${record.eventId}, " +
                "key=${record.partitionKey}: ${exception.message}\n" +
                "해당 키는 복구 전까지 블로킹됩니다."
            )
        }
}
```

**핸들러 개별** — 특정 핸들러만의 추가 동작(보상 로직 등):

```kotlin
override fun onEventDead(record: EventInbox, exception: Exception) {
    // 이 핸들러 타입에만 필요한 후처리
}
```

둘은 함께 동작한다(노티파이어 → 훅 순). 양쪽 모두 예외는 삼켜져 본 흐름을 깨지 않는다.

## 6. 운영

### 모니터링 쿼리

```sql
-- 재시도 대기 중 (자동 복구 예정 — 관찰만)
SELECT event_id, event_type, partition_key, attempt_count, last_attempted_at, error_message
FROM event_inbox WHERE status = 'FAILED' ORDER BY last_attempted_at;

-- 종결 실패 (키 블로킹 중 — 운영 대응 필요)
SELECT event_id, event_type, partition_key, attempt_count, error_message
FROM event_inbox WHERE status = 'DEAD' ORDER BY last_attempted_at;

-- 특정 키의 대기 백로그 (블로킹 영향 범위 확인)
SELECT event_id, event_type, status FROM event_inbox
WHERE partition_key = :key ORDER BY event_id;
```

### DEAD 복구 (수동)

원인을 수정한 뒤:

```sql
UPDATE event_inbox
SET status = 'RECEIVED', attempt_count = 0
WHERE event_id = :eventId;   -- 0.5초 내 폴러가 픽업, 그 키의 순서 유지한 채 재개
```

- `attempt_count = 0` 리셋을 잊으면 다음 실패 즉시 다시 DEAD 가 된다.
- 이벤트를 **포기하고 키만 풀고 싶으면**(순서 위반을 의식적으로 수용) 해당 행을
  DELETE 하거나 COMPLETED 로 마킹 — 이 결정은 자동화하지 않고 항상 사람이 한다.

### 하트비트 / stale 동작 요약

- 처리 중인 이벤트는 30초마다 `last_attempted_at` 갱신(인스턴스당 전용 데몬 스레드).
- stale 스캐너(60초 주기)는 "하트비트 120초 두절 + PROCESSING"만 RECEIVED 로 회수.
- → **처리 시간이 아무리 길어도 살아있는 처리는 회수되지 않고**, 인스턴스 사망 시
  약 2.5~3분 내 다른 인스턴스가 이어받는다(재처리 안전성은 핸들러 멱등성 담당).

## 7. 함정 모음

| 함정 | 증상 | 예방 |
|---|---|---|
| `PartitionedEvent` 미구현 | 전 이벤트 key=0 → 전역 직렬화(병렬성 소멸) | opt-in 전 partitionKey 확인 |
| 인덱스 미적용 | frontier/게이트 쿼리 풀스캔 | IObox.sql 인덱스 2개 실행 |
| 커넥션 풀 = 워커풀 | 커넥션 기근으로 처리 지연 | Hikari ≥ workerPoolSize + 여유 |
| SERIAL 인데 순서 기대 | SERIAL 은 순서 보장 안 함(폴 쿼리 무정렬 + 멀티 인스턴스) | 순서 필요하면 PARTITION_ORDERED |
| DEAD 복구 시 attempt_count 미리셋 | 복구 후 첫 실패에 즉시 재-DEAD | 복구 SQL 에 `attempt_count = 0` 포함 |
| 게이트는 키 전역 | 같은 key 면 **다른 eventType 이라도** 동시 처리 안 됨 | 의도된 동작(fail-safe) — 인과체인이면 영향 없음 |
