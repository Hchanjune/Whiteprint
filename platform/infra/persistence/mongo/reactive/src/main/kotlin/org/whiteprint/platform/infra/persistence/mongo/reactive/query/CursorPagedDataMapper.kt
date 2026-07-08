package org.whiteprint.platform.infra.persistence.mongo.reactive.query

import org.whiteprint.platform.core.projection.model.paging.cursor.Cursor
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorCodec
import org.whiteprint.platform.core.projection.model.paging.cursor.CursorPagedData
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams
import org.whiteprint.platform.core.projection.model.viewModel.ViewModel

/**
 * fetched는 toMongoQuery()의 limit(size+1)로 가져온 원본 결과여야 한다.
 * idOf/sortValueOf: 다음 커서를 만들기 위해 _id / 정렬필드 값을 꺼내는 함수.
 *
 * hasPreviousPage는 실제 역방향 존재 여부를 조회하지 않고 "커서가 있었으면 이전 페이지가 있다"로 근사한다 —
 * 정확도가 필요해지면 별도 확인 쿼리로 교체.
 */
fun <E, T: ViewModel> List<E>.toCursorPagedData(
    meta: CursorQueryParams,
    idOf: (E) -> String,
    sortValueOf: (E) -> String,
    mapper: (E) -> T,
): CursorPagedData<T> {
    val hasMore = size > meta.size
    val page = if (hasMore) take(meta.size) else this

    fun cursorOf(item: E) = CursorCodec.encode(Cursor(sortValueOf(item), idOf(item)))

    return CursorPagedData(
        content = page.map(mapper),
        size = meta.size,
        sort = meta.sort,
        meta = meta,
        hasNextPage = hasMore,
        hasPreviousPage = meta.cursor != null,
        startCursor = page.firstOrNull()?.let(::cursorOf),
        endCursor = page.lastOrNull()?.let(::cursorOf),
    )
}
