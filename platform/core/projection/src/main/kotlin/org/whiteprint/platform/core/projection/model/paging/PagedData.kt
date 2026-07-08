package org.whiteprint.platform.core.projection.model.paging

import org.whiteprint.platform.core.projection.model.query.QueryParams
import org.whiteprint.platform.core.projection.model.sort.Sort
import org.whiteprint.platform.core.projection.model.viewModel.ViewModel

interface PagedData<T: ViewModel> {
    val content: List<T>
    val size: Int
    val sort: Sort
    val hasNextPage: Boolean
    val hasPreviousPage: Boolean
    val meta: QueryParams

    val isEmpty: Boolean get() = content.isEmpty()
}