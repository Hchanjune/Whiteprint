package org.whiteprint.platform.core.projection.model.query.offset

import org.whiteprint.platform.core.projection.model.query.QueryParams
import org.whiteprint.platform.core.projection.model.sort.Sort

interface OffsetQueryParams : QueryParams {
    /** 1-based. 첫 페이지는 1. */
    val page: Int
    val size: Int
    val sort: Sort
}