package org.whiteprint.platform.infra.persistence.mongo.servlet.query

import java.util.Date
import org.bson.Document
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorPagedData
import org.whiteprint.platform.core.projection.model.paging.cursor.toCursorPagedData
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams
import org.whiteprint.platform.infra.persistence.mongo.common.query.cursorBoundaryCriteria
import org.whiteprint.platform.infra.persistence.mongo.common.query.toSpringSort

/**
 * 커서 검색 실행기 — 파이프라인 조립/커서 경계/정렬/limit(size+1)/totalCount count/커서 인코딩을
 * 전부 처리한다. 호출부에 남는 것은 필터 [Criteria], (필요 시) `$lookup` 등의 [preSortStages], mapper 뿐.
 *
 * 파이프라인: `match(filter) -> preSortStages -> match(커서 경계) -> sort -> limit(size+1)`
 *
 * 주의:
 * - **untyped aggregation이므로 filter/preSortStages의 필드명은 전부 raw(저장 필드명, snake_case 등)**.
 *   프로퍼티명은 매핑되지 않고 그대로 몽고에 전달되어 조용히 무시된다.
 * - totalCount는 filter만 적용해 count한다(커서 경계 제외 — 페이지 이동에도 값 유지).
 *   filter가 preSortStages의 계산 필드에 의존하게 되면 이 count는 틀리게 되므로 그땐 pipeline count로 확장할 것.
 * - **reactive 판과 달리 count와 page 조회가 순차 실행된다.** 라이브러리가 임의의 스레드풀을 만들어
 *   병렬화하면 트랜잭션 동기화/보안 컨텍스트/MDC 같은 ThreadLocal이 조용히 끊기고, 풀 크기도 소비자가
 *   제어할 수 없다. 그래서 순차를 택했다 — CONTAINS 검색처럼 두 쿼리 모두 컬렉션 스캔이면
 *   지연이 합산되므로, 그 경우엔 인덱스로 풀거나 reactive 스택을 쓸 것.
 * - 결과는 raw [Document]로 수신한다 — preSortStages의 계산 필드가 커서 경계값 인코딩에 필요하기 때문.
 *   타입 매핑까지 원하면 entityClass를 받는 오버로드를 쓴다.
 */
fun <T> MongoOperations.cursorSearch(
    params: CursorQueryParams,
    collectionName: String,
    filter: Criteria? = null,
    preSortStages: List<AggregationOperation> = emptyList(),
    mapper: (Document) -> T,
): CursorPagedData<T> {
    val countQuery = Query().apply { filter?.let(::addCriteria) }
    val totalCount = count(countQuery, collectionName)

    val stages = mutableListOf<AggregationOperation>()
    filter?.let { stages += Aggregation.match(it) }
    stages += preSortStages
    params.cursorBoundaryCriteria()?.let { stages += Aggregation.match(it) }
    stages += Aggregation.sort(params.toSpringSort())
    stages += Aggregation.limit((params.size + 1).toLong())

    val fetched = aggregate(Aggregation.newAggregation(stages), collectionName, Document::class.java).mappedResults

    return fetched.toCursorPagedData(
        params = params,
        totalCount = totalCount,
        idOf = { it.get("_id").toString() },
        sortValueOf = { it.cursorSortValue(params.sortBy.field) },
        mapper = mapper,
    )
}

/**
 * 타입 매핑 버전 — 컬렉션명은 [entityClass]에서 얻고, 결과 Document를 converter로 [entityClass]에 매핑해
 * mapper에 넘긴다. (내부 커서 인코딩은 매핑 전 raw Document에서 뽑으므로 계산 필드도 안전)
 */
fun <D : Any, T> MongoOperations.cursorSearch(
    params: CursorQueryParams,
    entityClass: Class<D>,
    filter: Criteria? = null,
    preSortStages: List<AggregationOperation> = emptyList(),
    mapper: (D) -> T,
): CursorPagedData<T> =
    cursorSearch(
        params = params,
        collectionName = getCollectionName(entityClass),
        filter = filter,
        preSortStages = preSortStages,
        mapper = { document -> mapper(converter.read(entityClass, document)) },
    )

/** 커서 경계값 인코딩용 — BSON Date는 ISO-8601(Instant)로 되돌려 SortValueType.INSTANT.parse와 짝을 맞춘다. */
private fun Document.cursorSortValue(field: String): String =
    when (val value = get(field)) {
        is Date -> value.toInstant().toString()
        else -> value.toString()
    }
