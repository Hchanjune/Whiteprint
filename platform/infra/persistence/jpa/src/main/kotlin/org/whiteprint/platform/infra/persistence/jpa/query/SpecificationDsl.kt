package org.whiteprint.platform.infra.persistence.jpa.query

import org.springframework.data.jpa.domain.Specification
import org.whiteprint.platform.core.projection.model.search.SearchKeyword

/** [RootEntity]의 소프트 삭제 플래그 **속성명**. `_id`와 달리 백킹 필드에 접두사가 없어 그대로 쓴다. */
const val SOFT_DELETE_ATTRIBUTE: String = "isDeleted"

/**
 * 검색 필터 조립 DSL(JPA). mongo의 `criteria { }`와 같은 역할이고 결과물만 [Specification]이다.
 * null 값은 자동 스킵되므로 옵셔널 필터의 null 체크 관용구가 사라진다.
 * 최상위 조건들은 AND로 묶이고, 빈 블록이면 전체 매치를 반환한다.
 *
 * ```kotlin
 * specification<UserEntity> {
 *     notDeleted(unless = includeDeleted)
 *     orGroup {
 *         eq("_id", query.idEq)
 *         keyword("nickname", query.nickname)
 *     }
 *     eq("status", query.statusEq)
 *     range("insertedAt", query.insertedFrom, query.insertedTo)
 *     custom(어떤Specification)   // DSL로 표현 안 되는 파생 필터의 escape hatch
 * }
 * ```
 *
 * ## mongo `criteria { }`와 다른 점 — 옮겨 쓸 때 반드시 볼 것
 *
 * - **필드명이 raw 저장 필드명이 아니라 엔티티 속성명이다.** `"inserted_at"`이 아니라 `"insertedAt"`.
 *   연관은 `"author.nickname"` 점 표기([resolvePath]) — **암묵적 inner join**이라 연관이 없는 행은
 *   결과에서 빠진다. outer join이 필요하면 [custom]으로 직접 조립할 것.
 * - **속성명이 틀리면 조용히 0건이 아니라 예외가 난다.** mongo의 untyped 파이프라인과 달리 메타모델이
 *   검증하므로, 이 함정만큼은 JPA가 더 안전하다.
 * - **대소문자**: JPA `like`는 DB collation을 따르고 mongo regex는 항상 case-sensitive →
 *   같은 검색어가 저장소별로 다른 결과를 낼 수 있다.
 *
 * ## mongo와 똑같이 남는 함정
 *
 * - **[orGroup]으로 전체를 감싸면 필터가 넓어진다.** `isDeleted`까지 OR에 섞여 삭제 행이 노출된다.
 *   검색어끼리만 OR, ID/날짜/boolean은 최상위 AND.
 * - **[eq]는 `Any?`를 받아 타입 방어가 없다.** [SearchKeyword] 객체를 그대로 넣으면 안 된다 — [keyword] 사용.
 * - **날짜/숫자에 [keyword] 금지.** `like`는 문자열 컬럼에만 의미가 있다. 범위는 [range].
 */
fun <T : Any> specification(block: SpecificationBuilder<T>.() -> Unit): Specification<T> =
    SpecificationBuilder<T>().apply(block).build()

@DslMarker
annotation class SpecificationDslMarker

@SpecificationDslMarker
class SpecificationBuilder<T : Any> internal constructor() {

    private val specifications = mutableListOf<Specification<T>>()

    private fun add(spec: Specification<T>) {
        specifications += spec
    }

    fun eq(attribute: String, value: Any?) {
        value?.let { v -> add(Specification { root, _, cb -> cb.equal(root.resolvePath<Any>(attribute), v) }) }
    }

    fun ne(attribute: String, value: Any?) {
        value?.let { v -> add(Specification { root, _, cb -> cb.notEqual(root.resolvePath<Any>(attribute), v) }) }
    }

    fun gt(attribute: String, value: Any?) {
        value?.let { v ->
            add(Specification { root, _, cb -> cb.greaterThan(root.resolvePath<Any>(attribute).comparable(), v.comparable()) })
        }
    }

    fun gte(attribute: String, value: Any?) {
        value?.let { v ->
            add(Specification { root, _, cb -> cb.greaterThanOrEqualTo(root.resolvePath<Any>(attribute).comparable(), v.comparable()) })
        }
    }

    fun lt(attribute: String, value: Any?) {
        value?.let { v ->
            add(Specification { root, _, cb -> cb.lessThan(root.resolvePath<Any>(attribute).comparable(), v.comparable()) })
        }
    }

    fun lte(attribute: String, value: Any?) {
        value?.let { v ->
            add(Specification { root, _, cb -> cb.lessThanOrEqualTo(root.resolvePath<Any>(attribute).comparable(), v.comparable()) })
        }
    }

    fun isIn(attribute: String, values: Collection<*>?) {
        values?.takeIf { it.isNotEmpty() }?.let { v ->
            add(Specification { root, _, _ -> root.resolvePath<Any>(attribute).`in`(v) })
        }
    }

    fun isNull(attribute: String, value: Boolean?) {
        value?.let { wantNull ->
            add(
                Specification { root, _, cb ->
                    val path = root.resolvePath<Any>(attribute)
                    if (wantNull) cb.isNull(path) else cb.isNotNull(path)
                },
            )
        }
    }

    /** [SearchKeyword]의 MatchMode(EQUALS/CONTAINS/STARTS_WITH/ENDS_WITH)를 반영한다. */
    fun keyword(attribute: String, keyword: SearchKeyword?) {
        keyword?.let { add(it.toSpecification(attribute)) }
    }

    /** from/to 어느 한쪽만 있어도 동작하는 폐구간 범위(gte/lte). 둘 다 null이면 스킵. */
    fun range(attribute: String, from: Any?, to: Any?) {
        gte(attribute, from)
        lte(attribute, to)
    }

    /** DSL로 표현되지 않는 파생 필터용 escape hatch — outer join, 서브쿼리 등. */
    fun custom(specification: Specification<T>?) {
        specification?.let(::add)
    }

    /** 블록 안의 조건들을 OR로 묶는다. 전부 스킵되어 비면 그룹 자체를 스킵. */
    fun orGroup(block: SpecificationBuilder<T>.() -> Unit) {
        SpecificationBuilder<T>()
            .apply(block)
            .specifications
            .reduceOrNull { acc, spec -> acc.or(spec) }
            ?.let(::add)
    }

    /** [RootEntity] 소프트 삭제 관례 필터. unless=true(삭제 포함)면 스킵. */
    fun notDeleted(unless: Boolean = false, attribute: String = SOFT_DELETE_ATTRIBUTE) {
        if (!unless) eq(attribute, false)
    }

    /**
     * 조합은 `Specification.and`/`or`에 맡긴다 — `toPredicate`를 직접 호출해 `cb.and(*predicates)`로
     * 엮으면 null 술어와 `CriteriaQuery` 널가능성을 호출부가 떠안게 되는데, 그 처리는 이미
     * Spring Data의 조합 로직 안에 있다.
     */
    internal fun build(): Specification<T> =
        specifications.reduceOrNull { acc, spec -> acc.and(spec) }
            // 빈 블록은 전체 매치 — mongo의 빈 Criteria와 같은 의미.
            ?: Specification { _, _, cb -> cb.conjunction() }
}
