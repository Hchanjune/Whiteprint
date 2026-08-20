package org.whiteprint.platform.core.domain.model.contract

/**
 * 상위 타입의 나열 순서가 구현체에서 멤버가 생성되는 순서다 —
 * `version, insertedAt, updatedAt, isDeleted, deletedAt`. 저장소별 기반 타입
 * (`Projection`, `ProjectionDocument`, `MongoDocument`, `RootEntity`)도 같은 순서로 선언되어 있으므로,
 * 여기 순서를 바꾸면 그쪽도 함께 맞출 것.
 */
interface Auditable : Versionable, Insertable, Updatable, Deletable
