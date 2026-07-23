package org.whiteprint.platform.core.projection.model.paging.cursor

import org.whiteprint.platform.core.projection.model.paging.PagedData

data class CursorPagedData<T>(
    override val content: List<T>,
    override val meta: CursorPageMeta,
    override val hasNextPage: Boolean,
    override val hasPreviousPage: Boolean,
    val startCursor: String?,
    val endCursor: String?,
): PagedData<T> {
    override val size: Int get() = meta.size
}

/** 페이지네이션 메타데이터는 그대로 두고 content 타입만 바꾼다 (예: Projection -> ViewModel). */
inline fun <T, R> CursorPagedData<T>.mapContent(transform: (T) -> R): CursorPagedData<R> =
    CursorPagedData(
        content = content.map(transform),
        meta = meta,
        hasNextPage = hasNextPage,
        hasPreviousPage = hasPreviousPage,
        startCursor = startCursor,
        endCursor = endCursor,
    )
