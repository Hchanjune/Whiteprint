package org.whiteprint.platform.core.projection.model.paging.cursor

import org.whiteprint.platform.core.projection.model.paging.PageMeta
import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import org.whiteprint.platform.core.projection.model.sort.SortDirection

/** 커서 페이지네이션 요청 값의 응답용 스냅샷. [sortBy]는 SortableField enum 이름(내부 필드명 아님). */
data class CursorPageMeta(
    val cursor: String?,
    val size: Int,
    val direction: CursorDirection,
    val sortBy: String,
    val sortDirection: SortDirection,
) : PageMeta
