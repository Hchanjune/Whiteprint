package org.whiteprint.platform.core.projection.model.query.cursor

import org.whiteprint.platform.core.projection.model.query.QueryParams
import org.whiteprint.platform.core.projection.model.sort.Sort

interface CursorQueryParams: QueryParams {
    val cursor: String?
    val size: Int
    val sort: Sort
    val direction: CursorDirection
}