package org.whiteprint.platform.core.projection.model.paging

/**
 * 페이지네이션 결과 계약. content 타입은 계층에 따라 달라진다 —
 * repository는 Projection을, 웹 어댑터는 ViewModel을 담고 `mapContent`로 변환한다.
 * (그래서 타입 바운드를 두지 않는다)
 *
 * [meta]는 Query 객체가 아니라 [PageMeta] 스냅샷만 허용된다. 그대로 직렬화해도 안전해야 한다.
 * 정렬 정보(sortBy/sortDirection)도 meta를 통해서만 노출된다.
 */
interface PagedData<T> {
    val content: List<T>
    val meta: PageMeta
    val hasNextPage: Boolean
    val hasPreviousPage: Boolean
    val size: Int

    val isEmpty: Boolean get() = content.isEmpty()
}
