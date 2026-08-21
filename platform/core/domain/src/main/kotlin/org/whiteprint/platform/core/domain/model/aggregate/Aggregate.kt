package org.whiteprint.platform.core.domain.model.aggregate

import org.whiteprint.platform.core.domain.model.contract.Auditable
import org.whiteprint.platform.core.domain.model.contract.Identifiable
import java.io.Serializable

/**
 * 선언 순서가 곧 구현체의 멤버 생성 순서다 —
 * `root, schemaVersion, id, version, insertedAt, updatedAt, isDeleted, deletedAt`.
 *
 * 애그리거잇 고유 식별 정보(root/schemaVersion/id)가 먼저 오고, 감사 필드 5종이 [Auditable] 계약의
 * 표준 순서로 뒤따른다. 앞의 셋은 이 클래스가 직접 선언하므로 여기 순서가, 뒤의 다섯은 상위 타입
 * 나열 순서가 결정한다 — 둘 중 하나만 바꾸면 순서가 어긋난다.
 *
 * 생명주기 훅(onCreate/onUpdate/onDelete/onRestore)은 두지 않는다. 훅을 부를 이음매가 없기 때문이다 —
 * [org.whiteprint.platform.core.domain.repository.AggregateRepository]는 계약만 있고 구현이 서비스마다
 * 손으로 작성되며, 특히 delete/restore 경로는 애그리거잇을 엔티티로 매핑하지 않아 훅이 바꾼 상태가
 * 저장될 길이 없다. 상태 전이는 애그리거잇의 **명시적 도메인 메서드**로 표현하고 애플리케이션 서비스가
 * 호출 시점을 정한다. (훅을 되살리려면 저장소 기반 구현부터 플랫폼이 제공해야 한다)
 */
abstract class Aggregate<ROOT: Any>:
    Identifiable<Serializable>,
    Auditable {
    abstract val root: ROOT
    abstract override val id: Serializable
    val aggregateType: String by lazy { root::class.simpleName ?: "UNKNOWN" }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aggregate<*>) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

}