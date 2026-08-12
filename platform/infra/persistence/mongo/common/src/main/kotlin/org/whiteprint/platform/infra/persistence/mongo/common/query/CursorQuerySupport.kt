package org.whiteprint.platform.infra.persistence.mongo.common.query

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorCodec
import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.springframework.data.domain.Sort as SpringSort

/**
 * 단일 정렬 + `_id` tie-breaker 커서 페이지네이션의 공통 조립.
 *
 * - find() 기반 조회: [toMongoQuery] 하나면 된다.
 * - aggregation 기반 조회($lookup 등): [cursorBoundaryCriteria]와 [toSpringSort]를
 *   파이프라인에 직접 끼운다 — `match(필터) -> lookup/addFields -> match(경계) -> sort -> limit(size+1)`.
 *   이때 sortBy.field는 파이프라인 상의 필드명(계산 필드 포함)이어야 한다.
 *
 * 경계값 타입 변환은 sortBy.valueType이 담당하므로 호출부에 타입 분기 코드가 필요 없다.
 * 결과는 limit(size+1) 원본 그대로 [toCursorPagedData]에 넘긴다.
 */
fun CursorQueryParams.toMongoQuery(): Query {
    val query = Query().with(toSpringSort()).limit(size + 1)
    cursorBoundaryCriteria()?.let(query::addCriteria)
    return query
}

/**
 * 정렬 필드 + `_id` tie-breaker. Query.with()와 Aggregation.sort() 양쪽에 그대로 쓸 수 있다.
 *
 * **논리 순서는 언제나 `(sortBy sortDirection, _id ASC)`이고, BACKWARD일 때만 그 역순으로 조회한다.**
 * 역순으로 조회해야 커서 "직전" size개를 얻을 수 있기 때문이다 — 논리 순서 그대로 자르면 이전 행 집합의
 * 앞에서 size개를 가져와 직전 페이지가 아니라 첫 페이지로 점프한다.
 * 되뒤집기는 [toCursorPagedData]가 하므로 호출부는 이 짝을 반드시 함께 써야 한다.
 */
fun CursorQueryParams.toSpringSort(): SpringSort {
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
                "_id",
            ),
        )
}

/** 커서가 없으면(첫 페이지) null. 경계값은 sortBy.valueType으로 파싱해 BSON 타입을 맞춘다. */
fun CursorQueryParams.cursorBoundaryCriteria(): Criteria? {
    val decoded = cursor?.let(CursorCodec::decode) ?: return null
    val boundaryValue = sortBy.valueType.parse(decoded.sortValue)

    // 정방향탐색+오름차순, 혹은 역방향탐색+내림차순이면 "더 큰 값" 방향으로 진행 -> gt, 그 반대는 lt
    val movingToGreater = (direction == CursorDirection.FORWARD) == (sortDirection == SortDirection.ASC)
    val primaryBoundary =
        if (movingToGreater) Criteria.where(sortBy.field).gt(boundaryValue)
        else Criteria.where(sortBy.field).lt(boundaryValue)

    // 경계는 **논리 순서**(tie-breaker가 항상 _id ASC) 기준으로 조립한다 — 조회 정렬이 뒤집히는 것과 무관하다.
    // 그래서 tie-break 비교는 primary 방향이 아니라 탐색 방향을 따른다.
    // (primary DESC + FORWARD에서 _id를 lt로 걸면 동점 그룹에서 중복/누락이 생긴다 — 실측으로 확인된 버그)
    val tieBreaker =
        if (direction == CursorDirection.FORWARD) Criteria.where("_id").gt(decoded.id)
        else Criteria.where("_id").lt(decoded.id)

    return Criteria().orOperator(
        primaryBoundary,
        Criteria().andOperator(
            Criteria.where(sortBy.field).`is`(boundaryValue),
            tieBreaker,
        ),
    )
}
