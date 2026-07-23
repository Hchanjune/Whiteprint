package org.whiteprint.platform.infra.persistence.mongo.reactive.query

import org.whiteprint.platform.core.projection.model.paging.cursor.Cursor
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorCodec
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorPagedData
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams

/**
 * fetched는 toMongoQuery()의 limit(size+1)로 가져온 원본 결과여야 한다.
 * idOf/sortValueOf: 다음 커서를 만들기 위해 _id / 정렬필드 값을 꺼내는 함수.
 *
 * meta에는 params 자체가 아니라 toMeta() 스냅샷이 실린다 — Query 객체의 내부 필드가
 * 응답으로 직렬화되는 것을 막기 위함.
 *
 * hasPreviousPage는 실제 역방향 존재 여부를 조회하지 않고 "커서가 있었으면 이전 페이지가 있다"로 근사한다 —
 * 정확도가 필요해지면 별도 확인 쿼리로 교체.
 */
fun <E, T> List<E>.toCursorPagedData(
    params: CursorQueryParams,
    idOf: (E) -> String,
    sortValueOf: (E) -> String,
    mapper: (E) -> T,
): CursorPagedData<T> {
    val hasMore = size > params.size
    val page = if (hasMore) take(params.size) else this

    fun cursorOf(item: E) = CursorCodec.encode(Cursor(sortValueOf(item), idOf(item)))

    return CursorPagedData(
        content = page.map(mapper),
        meta = params.toMeta(),
        hasNextPage = hasMore,
        hasPreviousPage = params.cursor != null,
        startCursor = page.firstOrNull()?.let(::cursorOf),
        endCursor = page.lastOrNull()?.let(::cursorOf),
    )
}
