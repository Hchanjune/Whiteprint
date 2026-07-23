package org.whiteprint.platform.core.projection.model.paging

/**
 * 페이지 응답에 실리는 페이지네이션 메타 스냅샷의 공통 계약.
 *
 * Query 객체를 그대로 meta로 노출하면 principal/필터 등 내부 필드가 직렬화되므로,
 * PagedData에는 반드시 이 스냅샷([CursorPageMeta][cursor.CursorPageMeta],
 * [OffsetPageMeta][offset.OffsetPageMeta])만 담는다.
 */
interface PageMeta
