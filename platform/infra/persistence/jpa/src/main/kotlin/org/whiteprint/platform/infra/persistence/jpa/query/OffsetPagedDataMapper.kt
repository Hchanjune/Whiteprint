package org.whiteprint.platform.infra.persistence.jpa.query

import org.springframework.data.domain.Page
import org.whiteprint.platform.core.projection.model.paging.offset.OffsetPagedData
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams
import org.whiteprint.platform.core.projection.model.viewModel.ViewModel

fun <E: Any, T: ViewModel> Page<E>.toOffsetPagedData(meta: OffsetQueryParams, mapper: (E) -> T): OffsetPagedData<T> =
    OffsetPagedData(
        content = content.map(mapper),
        size = meta.size,
        sort = meta.sort,
        meta = meta,
        currentPage = meta.page,
        totalCount = totalElements,
    )
