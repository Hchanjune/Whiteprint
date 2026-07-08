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