package org.whiteprint.platform.core.projection.model.paging.cursor

import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams

/**
 * 커서 조회 결과(limit `size+1`)를 [CursorPagedData]로 접는다. 저장소를 모르는 순수 로직이라 여기 산다 —
 * mongo/JPA 실행기가 모두 이 함수를 쓴다.
 *
 * fetched는 저장소별 `toSpringSort()` 정렬 + `limit(size + 1)`로 가져온 원본이어야 한다.
 * **이 짝은 분리할 수 없다** — BACKWARD는 정렬을 뒤집어 조회하고(그래야 커서 "직전" size개를 얻는다)
 * 여기서 논리 순서로 되뒤집기 때문이다. 한쪽만 쓰면 역방향 페이지가 뒤집힌 채 나온다.
 *
 * idOf/sortValueOf: 다음 커서를 만들기 위해 식별자 / 정렬필드 값을 꺼내는 함수.
 * **sortValueOf가 만든 문자열은 `sortBy.valueType.parse`로 되돌릴 수 있어야 한다** —
 * 이 왕복이 깨지면 다음 페이지 요청이 INVALID_CURSOR로 400이 난다.
 *
 * meta에는 params 자체가 아니라 toMeta() 스냅샷이 실린다 — Query 객체의 내부 필드가
 * 응답으로 직렬화되는 것을 막기 위함.
 *
 * next/previous 플래그는 탐색 방향에 따라 역할이 바뀐다. "한 건 더 가져와졌다"(hasMore)는 언제나
 * **진행 방향에 더 있다**는 뜻이고, 반대쪽은 "커서를 들고 왔으니 그쪽에 뭔가 있었다"로 근사한다.
 * (정확도가 필요해지면 반대쪽도 별도 확인 쿼리로 교체)
 */
fun <E, T> List<E>.toCursorPagedData(
    params: CursorQueryParams,
    /** 검색조건(필터)만 적용한 전체 건수 — 커서 경계는 제외하고 count해야 페이지 이동에도 값이 유지된다. */
    totalCount: Long,
    idOf: (E) -> String,
    sortValueOf: (E) -> String,
    mapper: (E) -> T,
): CursorPagedData<T> {
    val hasMore = size > params.size
    val trimmed = if (hasMore) take(params.size) else this
    val backward = params.direction == CursorDirection.BACKWARD
    val page = if (backward) trimmed.reversed() else trimmed

    fun cursorOf(item: E) = CursorCodec.encode(Cursor(sortValueOf(item), idOf(item)))

    return CursorPagedData(
        content = page.map(mapper),
        meta = params.toMeta(),
        totalCount = totalCount,
        hasNextPage = if (backward) params.cursor != null else hasMore,
        hasPreviousPage = if (backward) hasMore else params.cursor != null,
        startCursor = page.firstOrNull()?.let(::cursorOf),
        endCursor = page.lastOrNull()?.let(::cursorOf),
    )
}
