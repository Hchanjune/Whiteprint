package org.whiteprint.platform.core.projection.model.paging.cursor

import org.whiteprint.platform.core.projection.model.paging.PagedData
import org.whiteprint.platform.core.projection.model.query.cursor.CursorQueryParams
import org.whiteprint.platform.core.projection.model.sort.Sort
import org.whiteprint.platform.core.projection.model.viewModel.ViewModel

data class CursorPagedData<T: ViewModel>(
    override val content: List<T>,
    override val size: Int,
    override val sort: Sort,
    override val meta: CursorQueryParams,
    override val hasNextPage: Boolean,
    override val hasPreviousPage: Boolean,
    val startCursor: String?,
    val endCursor: String?,
): PagedData<T>

/** 페이지네이션 메타데이터는 그대로 두고 content 타입만 바꾼다 (예: Projection -> ViewModel). */
inline fun <T: ViewModel, R: ViewModel> CursorPagedData<T>.mapContent(transform: (T) -> R): CursorPagedData<R> =
    CursorPagedData(
        content = content.map(transform),
        size = size,
        sort = sort,
        meta = meta,
        hasNextPage = hasNextPage,
        hasPreviousPage = hasPreviousPage,
        startCursor = startCursor,
        endCursor = endCursor,
    )