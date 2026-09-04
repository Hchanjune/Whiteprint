package org.whiteprint.platform.infra.persistence.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.whiteprint.platform.core.domain.model.contract.Auditable
import java.time.Instant

/**
 * 다른 서비스가 소유한 형상의 **복제본** 기반 타입. mongo 의 `ProjectionDocument` 에 대응한다.
 *
 * [RootEntity] 와의 차이는 딱 두 가지이고, 둘 다 의도된 것이다.
 *
 * 1. **`@Version` 이 없다.** 여기의 [version] 은 정본이 실어 보낸 값을 *받아 적는* 것이라
 *    Hibernate 가 증가시키면 안 된다. 증가시키면 정본과 어긋나 이후 이벤트가
 *    stale 가드에 걸려 조용히 사라진다.
 * 2. **`delete()`/`restore()`/`touch()` 가 없다.** 복제본은 자체 상태 전이를 하지 않는다 —
 *    해제 이벤트가 와도 `is_deleted` 를 *받아 적을* 뿐이다. 그래서 감사 필드의 setter 가 열려 있다
 *    ([RootEntity] 는 `protected set`).
 *
 * **식별자는 여기서 선언하지 않는다.** 구체 엔티티가 `@Id` 를 직접 붙인다 —
 * 단일 키든, 여러 소유 도메인의 복제본을 한 테이블에 모으느라 `@IdClass` 로 복합 키를 쓰든
 * 같은 기반 타입을 쓸 수 있게 하기 위해서다.
 *
 * 쓰기는 [org.whiteprint.platform.infra.persistence.jpa.repository.OptimizedJpaRepository.upsertProjection]
 * 한 곳을 지난다. 감사 필드는 **컬럼명이 고정**이며(`version`, `inserted_at`, ...) 그 SQL 이 이를 전제한다.
 */
@MappedSuperclass
abstract class ProjectionEntity: Auditable {

    // 감사 필드 표준 순서: version, insertedAt, updatedAt, isDeleted, deletedAt (Auditable 계약과 동일)
    @Column(name = "version", nullable = false)
    override var version: Long = 0

    @Column(name = "inserted_at", nullable = false)
    override var insertedAt: Instant = Instant.EPOCH

    @Column(name = "updated_at", nullable = false)
    override var updatedAt: Instant = Instant.EPOCH

    @Column(name = "is_deleted", nullable = false)
    override var isDeleted: Boolean = false

    @Column(name = "deleted_at")
    override var deletedAt: Instant? = null

}
