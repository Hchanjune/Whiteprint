package org.whiteprint.platform.core.projection.model.paging.offset

import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams

/**
 * 오프셋 조회 결과를 [OffsetPagedData]로 접는다. 저장소를 모르는 순수 로직이라 여기 산다.
 *
 * fetched는 `skip(offset) + limit(size)`로 가져온 원본이어야 한다. 커서 쪽([toCursorPagedData])과 달리
 * `size + 1`을 더 가져오지 않는다 — hasNextPage/totalPages가 totalCount와 page로 계산되기 때문
 * ([OffsetPagedData]의 computed property).
 *
 * meta에는 params 자체가 아니라 toMeta() 스냅샷이 실린다 — Query 객체의 내부 필드가
 * 응답으로 직렬화되는 것을 막기 위함.
 */
fun <E, T> List<E>.toOffsetPagedData(
    params: OffsetQueryParams,
    /** 검색조건(필터)만 적용한 전체 건수. totalPages/hasNextPage 계산의 근거다. */
    totalCount: Long,
    mapper: (E) -> T,
): OffsetPagedData<T> =
    OffsetPagedData(
        content = map(mapper),
        meta = params.toMeta(),
        totalCount = totalCount,
    )
