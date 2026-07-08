package org.whiteprint.platform.infra.persistence.jpa.query

import org.whiteprint.platform.core.projection.model.sort.Sort
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.springframework.data.domain.Sort as SpringSort

fun Sort.toSpringSort(): SpringSort =
    SpringSort.by(
        orders.map {
            SpringSort.Order(
                if (it.direction == SortDirection.ASC) SpringSort.Direction.ASC else SpringSort.Direction.DESC,
                it.field,
            )
        },
    )
