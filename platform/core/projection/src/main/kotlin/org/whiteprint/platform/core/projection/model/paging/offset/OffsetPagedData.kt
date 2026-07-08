package org.whiteprint.platform.core.projection.model.paging.offset

import org.whiteprint.platform.core.projection.model.paging.PagedData
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams
import org.whiteprint.platform.core.projection.model.sort.Sort
import org.whiteprint.platform.core.projection.model.viewModel.ViewModel
import kotlin.math.ceil

data class OffsetPagedData<T: ViewModel>(
    override val content: List<T>,
    override val size: Int,
    override val sort: Sort,
    override val meta: OffsetQueryParams,
    /** 1-based. 첫 페이지는 1. */
    val currentPage: Int,
    val totalCount: Long,
): PagedData<T> {
    val totalPages: Int get() = if (size == 0) 0 else ceil(totalCount.toDouble() / size).toInt()
    override val hasNextPage: Boolean get() = currentPage < totalPages
    override val hasPreviousPage: Boolean get() = currentPage > 1
    val firstItemIndex: Long get() = if (totalCount == 0L) 0 else (currentPage - 1).toLong() * size + 1
    val lastItemIndex: Long get() = minOf(currentPage.toLong() * size, totalCount)
}