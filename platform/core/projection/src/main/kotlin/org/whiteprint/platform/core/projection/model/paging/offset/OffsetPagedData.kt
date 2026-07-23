package org.whiteprint.platform.core.projection.model.paging.offset

import org.whiteprint.platform.core.projection.model.paging.PagedData
import kotlin.math.ceil

data class OffsetPagedData<T>(
    override val content: List<T>,
    override val meta: OffsetPageMeta,
    override val totalCount: Long,
): PagedData<T> {
    override val size: Int get() = meta.size
    /** 1-based. 첫 페이지는 1. */
    val currentPage: Int get() = meta.page
    val totalPages: Int get() = if (size == 0) 0 else ceil(totalCount.toDouble() / size).toInt()
    override val hasNextPage: Boolean get() = currentPage < totalPages
    override val hasPreviousPage: Boolean get() = currentPage > 1
    val firstItemIndex: Long get() = if (totalCount == 0L) 0 else (currentPage - 1).toLong() * size + 1
    val lastItemIndex: Long get() = minOf(currentPage.toLong() * size, totalCount)
}

/** 페이지네이션 메타데이터는 그대로 두고 content 타입만 바꾼다 (예: Projection -> ViewModel). */
inline fun <T, R> OffsetPagedData<T>.mapContent(transform: (T) -> R): OffsetPagedData<R> =
    OffsetPagedData(
        content = content.map(transform),
        meta = meta,
        totalCount = totalCount,
    )
