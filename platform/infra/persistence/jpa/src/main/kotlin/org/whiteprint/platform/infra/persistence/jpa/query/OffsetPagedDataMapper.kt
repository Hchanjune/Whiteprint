package org.whiteprint.platform.infra.persistence.jpa.query

import org.springframework.data.domain.Page
import org.whiteprint.platform.core.projection.model.paging.offset.OffsetPagedData
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams

fun <E: Any, T> Page<E>.toOffsetPagedData(params: OffsetQueryParams, mapper: (E) -> T): OffsetPagedData<T> =
    OffsetPagedData(
        content = content.map(mapper),
        meta = params.toMeta(),
        totalCount = totalElements,
    )
