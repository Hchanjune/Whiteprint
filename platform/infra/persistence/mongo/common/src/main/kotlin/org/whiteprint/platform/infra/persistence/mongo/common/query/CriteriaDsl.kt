package org.whiteprint.platform.infra.persistence.mongo.common.query

import org.springframework.data.mongodb.core.query.Criteria
import org.whiteprint.platform.core.projection.model.search.SearchKeyword

/**
 * 검색 필터 조립 DSL. null 값은 자동 스킵되므로 옵셔널 필터의 null 체크 관용구가 사라진다.
 * 최상위 조건들은 AND로 묶이고, 빈 블록이면 빈 Criteria(전체 매치)를 반환한다.
 *
 * ```kotlin
 * criteria {
 *     notDeleted(unless = includeDeleted)
 *     orGroup {
 *         eq("_id", query.idEq)
 *         keyword("title", query.title)
 *     }
 *     eq("genre", query.genreEq)
 *     range("inserted_at", query.insertedFrom, query.insertedTo)
 *     custom(어떤Criteria)   // DSL로 표현 안 되는 파생 필터의 escape hatch
 * }
 * ```
 *
 * 주의: cursorSearch의 untyped 파이프라인에서 쓰이므로 필드명은 raw(저장 필드명)여야 한다.
 */
fun criteria(block: CriteriaBuilder.() -> Unit): Criteria = CriteriaBuilder().apply(block).build()

@DslMarker
annotation class CriteriaDslMarker

@CriteriaDslMarker
class CriteriaBuilder internal constructor() {

    private val criteriaList = mutableListOf<Criteria>()

    fun eq(field: String, value: Any?) {
        value?.let { criteriaList += Criteria.where(field).`is`(it) }
    }

    fun ne(field: String, value: Any?) {
        value?.let { criteriaList += Criteria.where(field).ne(it) }
    }

    fun gt(field: String, value: Any?) {
        value?.let { criteriaList += Criteria.where(field).gt(it) }
    }

    fun gte(field: String, value: Any?) {
        value?.let { criteriaList += Criteria.where(field).gte(it) }
    }

    fun lt(field: String, value: Any?) {
        value?.let { criteriaList += Criteria.where(field).lt(it) }
    }

    fun lte(field: String, value: Any?) {
        value?.let { criteriaList += Criteria.where(field).lte(it) }
    }

    fun isIn(field: String, values: Collection<*>?) {
        values?.takeIf { it.isNotEmpty() }?.let { criteriaList += Criteria.where(field).`in`(it) }
    }

    /** [SearchKeyword]의 MatchMode(EQUALS/CONTAINS/STARTS_WITH/ENDS_WITH)를 반영한다. */
    fun keyword(field: String, keyword: SearchKeyword?) {
        keyword?.let { criteriaList += it.toCriteria(field) }
    }

    /** from/to 어느 한쪽만 있어도 동작하는 폐구간 범위(gte/lte). 둘 다 null이면 스킵. */
    fun range(field: String, from: Any?, to: Any?) {
        if (from == null && to == null) return
        val range = Criteria.where(field)
        from?.let { range.gte(it) }
        to?.let { range.lte(it) }
        criteriaList += range
    }

    /** DSL로 표현되지 않는 파생 필터용 escape hatch. */
    fun custom(criteria: Criteria?) {
        criteria?.let { criteriaList += it }
    }

    /** 블록 안의 조건들을 OR로 묶는다. 전부 스킵되어 비면 그룹 자체를 스킵. */
    fun orGroup(block: CriteriaBuilder.() -> Unit) {
        val inner = CriteriaBuilder().apply(block).criteriaList
        if (inner.isNotEmpty()) {
            criteriaList += Criteria().orOperator(*inner.toTypedArray())
        }
    }

    /** ProjectionDocument 소프트 삭제 관례(`is_deleted`) 필터. unless=true(삭제 포함)면 스킵. */
    fun notDeleted(unless: Boolean = false) {
        if (!unless) {
            criteriaList += Criteria.where("is_deleted").`is`(false)
        }
    }

    internal fun build(): Criteria =
        if (criteriaList.isEmpty()) Criteria() else Criteria().andOperator(*criteriaList.toTypedArray())
}
