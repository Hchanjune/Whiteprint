package org.whiteprint.platform.core.projection.model.paging.offset

import org.whiteprint.platform.core.projection.model.paging.PageMeta
import org.whiteprint.platform.core.projection.model.sort.SortDirection

/** 오프셋 페이지네이션 요청 값의 응답용 스냅샷. [sortBy]는 SortableField enum 이름(내부 필드명 아님). */
data class OffsetPageMeta(
    /** 1-based. 첫 페이지는 1. */
    val page: Int,
    val size: Int,
    val sortBy: String,
    val sortDirection: SortDirection,
) : PageMeta
