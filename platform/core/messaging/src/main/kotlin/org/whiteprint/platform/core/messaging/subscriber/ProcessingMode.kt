package org.whiteprint.platform.core.messaging.subscriber

/**
 * 인박스 이벤트 핸들러의 처리 모드.
 *
 * 상세 설계: `platform/adapter/event/inbox/partition-ordered-design.md`
 */
enum class ProcessingMode {

    /**
     * 현행 기본 — 폴 스레드에서 배치를 직렬 인라인 처리.
     * 순서는 보장하지 않으며(폴 쿼리 무정렬), 인스턴스 내 동시 처리도 없다.
     */
    SERIAL,

    /**
     * 경쟁 소비 — event 단위 claim 후 워커풀에서 병렬 처리.
     * 순서를 보장하지 않으므로 순서 무관·멱등 핸들러에만 사용할 것.
     */
    PARALLEL,

    /**
     * 파티션 키 순서 보장 — 같은 partition_key 는 전체 인스턴스를 통틀어
     * 한 번에 1건씩 시간순으로, 다른 키는 병렬로 처리한다.
     * FAILED 이벤트는 해당 키를 블로킹한다(복구 전까지 후속 이벤트 정지).
     */
    PARTITION_ORDERED,
}
