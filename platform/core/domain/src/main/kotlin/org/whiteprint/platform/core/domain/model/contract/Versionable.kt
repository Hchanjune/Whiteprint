package org.whiteprint.platform.core.domain.model.contract

/**
 * 낙관적 잠금 / 프로젝션 stale 판정에 쓰이는 형상 버전.
 *
 * [Updatable]에서 분리한 이유는 두 가지다.
 * - 버전은 "언제 바뀌었나"(updatedAt)와 다른 관심사다 — 쓰기 충돌 감지와 읽기측 반영 순서 판정에 쓰인다.
 * - [Auditable]의 상위 타입 **순서**가 곧 구현체의 멤버 생성 순서라, 버전을 맨 앞에 두려면
 *   독립 계약이어야 한다. 감사 필드의 표준 순서는 `version, insertedAt, updatedAt, isDeleted, deletedAt`.
 */
interface Versionable {
    val version: Long
}
