package org.whiteprint.platform.core.messaging.subscriber

/**
 * 인박스 이벤트 핸들러의 처리 모드.
 *
 * 멀티 인스턴스(무상태 복제본) 기준 선택: 순서 무관·멱등이면 [PARALLEL], 같은 키를 한 번에 하나씩 처리하고
 * 앞선 실패 시 뒤를 멈춰야 하면 [PARTITION_ORDERED]. [SERIAL] 은 하위호환 기본값일 뿐 정합성 수단이 아니다.
 *
 * 사용 가이드: `platform/adapter/event/inbox/partition-ordered-usage.md`
 * 설계 기록: `platform/adapter/event/inbox/partition-ordered-design.md`
 */
enum class ProcessingMode {

    /**
     * 하위호환 기본값 — 인스턴스마다 폴 스레드가 event_id 순으로 배치를 가져와 claim 후 인라인 처리한다.
     *
     * **한 번에 1건은 인스턴스 안에서만이다.** 복제본이 2대 이상이면 각 인스턴스가 따로 가져가 처리하므로
     * 인스턴스끼리는 병렬이고, 순서도 전체 직렬도 보장하지 않는다. 실패 건은 backoff 뒤 재시도되므로
     * 단일 인스턴스에서도 순서가 뒤집힌다.
     *
     * 사실상 [PARALLEL] + `workerPoolSize = 1` 과 같다(차이는 폴 스레드가 처리 동안 묶인다는 것뿐).
     * 정합성 수단으로 쓰지 말 것 — 동시성을 줄이고 싶으면 PARALLEL 의 `workerPoolSize` 로 조절한다.
     */
    SERIAL,

    /**
     * 경쟁 소비 — event 단위 claim 후 워커풀에서 병렬 처리(인스턴스당 최대 `workerPoolSize` 건).
     * 순서를 보장하지 않으므로 순서 무관·멱등 핸들러에만 사용할 것
     * (버전 조건 upsert 프로젝션, 다시 계산하는 집계, 유니크로 막히는 생성 등).
     */
    PARALLEL,

    /**
     * 파티션 키 단위 직렬 — 같은 partition_key 는 전체 인스턴스를 통틀어 한 번에 1건, 다른 키는 병렬.
     *
     * - **처리 순서(event_id 순)는 같은 이벤트 타입 안에서만 보장된다.** 대기 중인 이벤트를 고르는 조회가
     *   핸들러의 event_type 으로 걸러지기 때문이다. 같은 키의 서로 다른 타입끼리는 동시에만 안 돌 뿐 순서는 모른다.
     * - 반면 **막는 조건은 이벤트 타입과 무관하다** — 같은 키에 PROCESSING·FAILED·DEAD 가 하나라도 있으면
     *   그 키의 다른 타입 이벤트도 멈춘다(복구 전까지 후속 정지). 인과 사슬이면 의도된 동작이다.
     * - 그래서 재처리에 실패하는(멱등이 아닌) 핸들러에 쓰면 DEAD 한 건이 그 키를 영구 정지시킨다 — 멱등으로 만든 뒤 쓸 것.
     */
    PARTITION_ORDERED,
}
