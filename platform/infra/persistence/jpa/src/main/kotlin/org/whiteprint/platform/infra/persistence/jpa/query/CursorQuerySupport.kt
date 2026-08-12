package org.whiteprint.platform.infra.persistence.jpa.query

import jakarta.persistence.criteria.Path
import org.springframework.data.jpa.domain.Specification
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorCodec
import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.whiteprint.platform.core.projection.policy.QueryException
import org.whiteprint.platform.core.projection.policy.QueryPolicy
import org.springframework.data.domain.Sort as SpringSort

/**
 * 단일 정렬 + 식별자 tie-breaker 커서 페이지네이션의 JPA 조립.
 * mongo 판([org.whiteprint.platform.infra.persistence.mongo.reactive.query] 참조)과 의미는 같고
 * 조립 대상만 [Criteria] 대신 [Specification]이다.
 *
 * mongo와 다른 점:
 * - `preSortStages` 상당이 없다. 조인은 [Specification] 안에서 표현하고, 정렬/경계가 조인 컬럼을 가리켜야 하면
 *   `sortBy.field`에 점 표기를 쓴다([resolvePath]).
 * - tie-breaker 속성명이 `"_id"` 리터럴이 아니라 [ENTITY_ID_ATTRIBUTE] 기본값이다.
 *   같은 철자지만 근거가 다르다 — mongo는 BSON 실제 필드명, JPA는 [BaseEntity]의 백킹 필드명이다.
 *   (Hibernate는 `"id"`를 식별자 별칭으로도 받아준다 — 실측 확인. 둘 다 같은 결과를 낸다)
 *
 * **논리 순서는 언제나 `(sortBy sortDirection, 식별자 ASC)`이고, BACKWARD일 때만 그 역순으로 조회한다.**
 * 역순으로 조회해야 커서 "직전" size개를 얻는다 — 논리 순서 그대로 자르면 이전 행 집합의 앞에서 size개를
 * 가져와 직전 페이지가 아니라 첫 페이지로 점프한다. 되뒤집기는 [toCursorPagedData]가 한다.
 */
fun CursorQueryParams.toSpringSort(tieBreakerAttribute: String = ENTITY_ID_ATTRIBUTE): SpringSort {
    val backward = direction == CursorDirection.BACKWARD
    val primaryAscending = (sortDirection == SortDirection.ASC) != backward
    return SpringSort
        .by(
            if (primaryAscending) SpringSort.Direction.ASC else SpringSort.Direction.DESC,
            sortBy.field,
        )
        .and(
            SpringSort.by(
                if (backward) SpringSort.Direction.DESC else SpringSort.Direction.ASC,
                tieBreakerAttribute,
            ),
        )
}

/**
 * 커서가 없으면(첫 페이지) null. 조립되는 술어는
 * `sortValue > x OR (sortValue = x AND id > y)` (진행 방향에 따라 부등호가 뒤집힘).
 *
 * 경계값은 `sortBy.valueType`이 복원하지만 **식별자는 [idParser]로 호출부가 복원해야 한다.**
 * 메타모델에 물어볼 수 없기 때문이다 — [BaseEntity]가 `ID : Serializable` 제네릭이라
 * `_id` 경로의 `javaType`이 지운 타입인 `java.io.Serializable`로 나온다(실측 확인).
 * `idOf`가 인코딩, [idParser]가 디코딩으로 짝을 이룬다.
 */
fun <T : Any> CursorQueryParams.cursorBoundarySpecification(
    idParser: (String) -> Comparable<*>,
    tieBreakerAttribute: String = ENTITY_ID_ATTRIBUTE,
): Specification<T>? {
    val decoded = cursor?.let(CursorCodec::decode) ?: return null
    val boundaryValue = sortBy.valueType.parse(decoded.sortValue)
    val boundaryId = decoded.id.parseIdWith(idParser)

    // 정방향탐색+오름차순, 혹은 역방향탐색+내림차순이면 "더 큰 값" 방향으로 진행 -> gt, 그 반대는 lt
    val movingToGreater = (direction == CursorDirection.FORWARD) == (sortDirection == SortDirection.ASC)

    return Specification { root, _, cb ->
        val sortPath = root.resolvePath<Any>(sortBy.field)
        val idPath = root.resolvePath<Any>(tieBreakerAttribute)

        val primaryBoundary =
            if (movingToGreater) cb.greaterThan(sortPath.comparable(), boundaryValue.comparable())
            else cb.lessThan(sortPath.comparable(), boundaryValue.comparable())

        // tie-break 비교는 primary 방향이 아니라 탐색 방향을 따른다 — 식별자 정렬이 항상 ASC 고정이기 때문.
        // (primary DESC + FORWARD에서 식별자를 lt로 걸면 동점 그룹에서 중복/누락이 생긴다)
        val tieBreaker =
            if (direction == CursorDirection.FORWARD) cb.greaterThan(idPath.comparable(), boundaryId.comparable())
            else cb.lessThan(idPath.comparable(), boundaryId.comparable())

        cb.or(
            primaryBoundary,
            cb.and(cb.equal(sortPath, boundaryValue), tieBreaker),
        )
    }
}

/**
 * 호출부 [idParser]의 실패는 변조되었거나 다른 엔티티로 만들어진 커서다 —
 * `SortValueType.parse`와 동일하게 INVALID_CURSOR(400)로 감싼다. 그대로 두면 NumberFormatException이
 * 500으로 새어 나간다.
 */
private fun String.parseIdWith(idParser: (String) -> Comparable<*>): Comparable<*> = try {
    idParser(this)
} catch (e: Exception) {
    throw QueryException(QueryPolicy.INVALID_CURSOR, mapOf("key" to "id", "value" to this), cause = e)
}

