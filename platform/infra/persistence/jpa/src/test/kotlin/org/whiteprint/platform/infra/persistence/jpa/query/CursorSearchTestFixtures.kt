package org.whiteprint.platform.infra.persistence.jpa.query

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.whiteprint.platform.core.projection.model.sort.SortValueType
import org.whiteprint.platform.core.projection.model.sort.SortableField
import org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity

@SpringBootApplication
class JpaCursorTestApplication

/**
 * [RootEntity]를 상속한다 — 식별자 매핑(`@Id private var _id`)과 소프트 삭제 속성(`isDeleted`)이
 * 둘 다 검증 대상이라 여기서 재정의하면 안 된다.
 * [score]는 **의도적으로 동점이 많이 나오도록** 채워서 tie-breaker를 시험한다.
 */
@Entity
@Table(name = "cursor_test_items")
class CursorTestItem(
    @Column(name = "score")
    var score: Long = 0,

    @Column(name = "label")
    var label: String = "",
) : RootEntity<Long>() {

    @get:Transient
    override val useSoftDelete: Boolean get() = true
}

interface CursorTestItemRepository :
    JpaRepository<CursorTestItem, Long>,
    JpaSpecificationExecutor<CursorTestItem>

/** `field`는 엔티티 **속성명**이다(컬럼명 아님) — mongo와 의미가 다른 지점. */
enum class CursorTestSortField(
    override val field: String,
    override val valueType: SortValueType,
) : SortableField {
    SCORE("score", SortValueType.LONG),
}

data class CursorTestParams(
    override val cursor: String? = null,
    override val size: Int = 3,
    override val sortBy: CursorTestSortField = CursorTestSortField.SCORE,
    override val sortDirection: SortDirection = SortDirection.DESC,
    override val direction: CursorDirection = CursorDirection.FORWARD,
) : CursorQueryParams

data class OffsetTestParams(
    override val page: Int = 1,
    override val size: Int = 3,
    override val sortBy: CursorTestSortField = CursorTestSortField.SCORE,
    override val sortDirection: SortDirection = SortDirection.DESC,
) : OffsetQueryParams
