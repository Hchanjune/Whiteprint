package org.whiteprint.platform.infra.persistence.mongo.common.query

import org.springframework.data.mongodb.core.query.Query
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.springframework.data.domain.Sort as SpringSort

/**
 * 단일 정렬 + `_id` tie-breaker 오프셋 페이지네이션의 공통 조립. [CursorQuerySupport]와 대칭이다.
 *
 * - find() 기반 조회: [toMongoQuery] 하나면 된다.
 * - aggregation 기반 조회($lookup 등): [toSpringSort]를 파이프라인에 직접 끼운다 —
 *   `match(필터) -> lookup/addFields -> sort -> skip -> limit(size)`.
 *   이때 sortBy.field는 파이프라인 상의 필드명(계산 필드 포함)이어야 한다.
 *
 * 커서 방식과 달리 경계 조건이 없으므로 [org.whiteprint.platform.core.projection.model.sort.SortValueType]은
 * 쓰이지 않는다 — 정렬 필드의 값 타입을 몰라도 된다.
 */
fun OffsetQueryParams.toMongoQuery(): Query =
    Query().with(toSpringSort()).skip(offset).limit(size)

/**
 * 정렬 필드 + `_id` ASC tie-breaker. Query.with()와 Aggregation.sort() 양쪽에 그대로 쓸 수 있다.
 *
 * tie-breaker는 오프셋에서도 필수다. skip/limit은 매 요청 정렬을 처음부터 다시 하므로, 정렬키에 동점이
 * 있으면 요청마다 동점 그룹 내 순서가 달라져 같은 문서가 두 페이지에 겹쳐 나오거나 아예 누락된다.
 * (커서 쪽 tie-breaker와 같은 이유이고, 두 방식의 정렬 결과를 일치시키는 역할도 한다)
 */
fun OffsetQueryParams.toSpringSort(): SpringSort =
    SpringSort
        .by(
            if (sortDirection == SortDirection.ASC) SpringSort.Direction.ASC else SpringSort.Direction.DESC,
            sortBy.field,
        )
        .and(SpringSort.by(SpringSort.Direction.ASC, "_id"))
