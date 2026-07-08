package org.whiteprint.platform.infra.persistence.mongo.reactive.query

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorCodec
import org.springframework.data.domain.Sort as SpringSort

/**
 * 스켈레톤: 정렬 기준 1개 + `_id` tie-breaker 조합만 지원한다 (복합 정렬 커서는 추후 확장 포인트).
 * limit을 size+1로 걸어서 실제 more-data 여부(hasNextPage)를 count 쿼리 없이 판별한다.
 */
fun CursorQueryParams.toMongoQuery(): Query {
    val primary = sort.orders.firstOrNull()
        ?: error("Cursor pagination requires exactly one sort field for this skeleton")

    val ascending = primary.direction == SortDirection.ASC
    val forward = direction == CursorDirection.FORWARD
    // 정방향탐색+오름차순, 혹은 역방향탐색+내림차순이면 "더 큰 값" 방향으로 진행 -> gt, 그 반대는 lt
    val movingToGreater = forward == ascending

    val springSort = SpringSort
        .by(if (ascending) SpringSort.Direction.ASC else SpringSort.Direction.DESC, primary.field)
        .and(SpringSort.by(SpringSort.Direction.ASC, "_id"))

    val query = Query().with(springSort).limit(size + 1)

    cursor?.let(CursorCodec::decode)?.let { decoded ->
        val boundary = if (movingToGreater) {
            Criteria().orOperator(
                Criteria.where(primary.field).gt(decoded.sortValue),
                Criteria().andOperator(
                    Criteria.where(primary.field).`is`(decoded.sortValue),
                    Criteria.where("_id").gt(decoded.id),
                ),
            )
        } else {
            Criteria().orOperator(
                Criteria.where(primary.field).lt(decoded.sortValue),
                Criteria().andOperator(
                    Criteria.where(primary.field).`is`(decoded.sortValue),
                    Criteria.where("_id").lt(decoded.id),
                ),
            )
        }
        query.addCriteria(boundary)
    }

    return query
}
