package org.whiteprint.platform.adapter.event.inbox.configuration.mongo.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

/**
 * PARTITION_ORDERED claim 의 키 단위 상호배제 락(Postgres advisory lock 의 Mongo 대응).
 *
 * - 락 획득 = 이 문서의 insert 시도 — `_id`(partitionKey) 유니크가 원자성을 담당하고,
 *   DuplicateKey 는 "타 인스턴스가 같은 키를 claim 중"을 의미한다.
 * - claim 이 끝나면(성공/실패 불문) 즉시 delete — 락 문서는 밀리초 수명이다.
 * - 크래시로 delete 가 누락된 잔여 문서는 [lockedAt] TTL 인덱스가 자동 청소한다
 *   (스토어가 인덱스를 보장 — MongoEventInboxStore 참고).
 *
 * Mongo 멀티도큐먼트 트랜잭션은 스냅샷 읽기 특성상 "서로 다른 문서를 쓰는 두 claim"의
 * 충돌을 감지하지 못하므로, 이 락 컬렉션 방식이 게이트 레이스를 닫는 유일한 수단이다.
 */
@Document("event_inbox_key_locks")
class EventInboxKeyLockDocument(

    @Id
    val partitionKey: Long,

    @Field("locked_at")
    val lockedAt: Instant,
)
