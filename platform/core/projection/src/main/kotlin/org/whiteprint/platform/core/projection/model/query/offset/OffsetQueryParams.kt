package org.whiteprint.platform.core.projection.model.query.offset

import org.whiteprint.platform.core.projection.model.paging.offset.OffsetPageMeta
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.whiteprint.platform.core.projection.model.sort.SortableField

/**
 * 오프셋 페이지네이션 필드 계약. QueryParams(웹 바인딩)와 Query(port.in)가 **각각** 구현한다.
 * 이 인터페이스는 페이지네이션 능력만 표현하며 QueryParams/Query 마커 어느 쪽도 상속하지 않는다 —
 * 한 타입이 두 계층을 관통하는 것을 막기 위함.
 *
 * [sortBy]는 서비스별 [SortableField] enum으로 선언한다(covariant override).
 * 웹 Params에서 enum 바인딩이 곧 화이트리스트 검증이고, 기본값이 곧 fallback 정렬이다.
 */
interface OffsetQueryParams {
    /** 1-based. 첫 페이지는 1. */
    val page: Int
    val size: Int
    val sortBy: SortableField
    val sortDirection: SortDirection

    /** 저장소에 넘길 0-based 시작 인덱스. [page]가 1-based이므로 여기서 한 번만 변환한다. */
    val offset: Long get() = (page - 1).toLong() * size

    /** 응답(PagedData.meta)에 실어도 안전한 스냅샷으로 변환한다. 내부 필드명 대신 wire name(kebab)을 에코한다. */
    fun toMeta() = OffsetPageMeta(page, size, sortBy.paramName, sortDirection)
}
