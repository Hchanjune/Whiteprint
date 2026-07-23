package org.whiteprint.platform.infra.persistence.jpa.query

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.whiteprint.platform.core.projection.model.query.offset.OffsetQueryParams
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.springframework.data.domain.Sort as SpringSort

/** OffsetQueryParams.page는 1-based, Spring Data Pageable은 0-based라 여기서 한 번 변환한다. */
fun OffsetQueryParams.toPageable(): Pageable =
    PageRequest.of(
        page - 1,
        size,
        SpringSort.by(
            if (sortDirection == SortDirection.ASC) SpringSort.Direction.ASC else SpringSort.Direction.DESC,
            sortBy.field,
        ),
    )
