package org.whiteprint.platform.infra.persistence.mongo.servlet.query

import org.bson.Document
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.whiteprint.platform.core.projection.model.paging.offset.OffsetPagedData
import org.whiteprint.platform.core.projection.model.paging.offset.toOffsetPagedData
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams
import org.whiteprint.platform.infra.persistence.mongo.common.query.toSpringSort

/**
 * 오프셋 검색 실행기 — 파이프라인 조립/정렬/skip/limit/totalCount count를 전부 처리한다.
 * 호출부에 남는 것은 필터 [Criteria], (필요 시) `$lookup` 등의 [preSortStages], mapper 뿐.
 * [cursorSearch]와 대칭이며, 다른 점은 커서 경계 대신 skip을 쓴다는 것 하나다.
 *
 * 파이프라인: `match(filter) -> preSortStages -> sort -> skip -> limit(size)`
 *
 * 주의:
 * - **untyped aggregation이므로 filter/preSortStages의 필드명은 전부 raw(저장 필드명, snake_case 등)**.
 *   프로퍼티명은 매핑되지 않고 그대로 몽고에 전달되어 조용히 무시된다.
 * - `$skip`은 건너뛴 문서를 서버가 전부 읽고 버린다 — 깊은 페이지일수록 선형으로 느려진다.
 *   임의 페이지 점프가 필요한 어드민 화면용이고, 무한스크롤/피드에는 [cursorSearch]를 쓸 것.
 * - totalCount는 filter만 적용해 count한다. filter가 preSortStages의 계산 필드에 의존하게 되면
 *   이 count는 틀리게 되므로 그땐 pipeline count로 확장할 것.
 * - **reactive 판과 달리 count와 page 조회가 순차 실행된다.** 이유는 [cursorSearch] 주석 참조.
 * - 결과는 raw [Document]로 수신한다. 타입 매핑까지 원하면 entityClass를 받는 오버로드를 쓴다.
 */
fun <T> MongoOperations.offsetSearch(
    params: OffsetQueryParams,
    collectionName: String,
    filter: Criteria? = null,
    preSortStages: List<AggregationOperation> = emptyList(),
    mapper: (Document) -> T,
): OffsetPagedData<T> {
    val countQuery = Query().apply { filter?.let(::addCriteria) }
    val totalCount = count(countQuery, collectionName)

    val stages = mutableListOf<AggregationOperation>()
    filter?.let { stages += Aggregation.match(it) }
    stages += preSortStages
    stages += Aggregation.sort(params.toSpringSort())
    stages += Aggregation.skip(params.offset)
    stages += Aggregation.limit(params.size.toLong())

    val fetched = aggregate(Aggregation.newAggregation(stages), collectionName, Document::class.java).mappedResults

    return fetched.toOffsetPagedData(
        params = params,
        totalCount = totalCount,
        mapper = mapper,
    )
}

/**
 * 타입 매핑 버전 — 컬렉션명은 [entityClass]에서 얻고, 결과 Document를 converter로 [entityClass]에 매핑해
 * mapper에 넘긴다.
 */
fun <D : Any, T> MongoOperations.offsetSearch(
    params: OffsetQueryParams,
    entityClass: Class<D>,
    filter: Criteria? = null,
    preSortStages: List<AggregationOperation> = emptyList(),
    mapper: (D) -> T,
): OffsetPagedData<T> =
    offsetSearch(
        params = params,
        collectionName = getCollectionName(entityClass),
        filter = filter,
        preSortStages = preSortStages,
        mapper = { document -> mapper(converter.read(entityClass, document)) },
    )
