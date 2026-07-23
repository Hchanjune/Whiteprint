package org.whiteprint.platform.core.projection.model.query

/**
 * 애플리케이션 port.in의 조회 입력 계약. (command 사이드의 Command와 대칭)
 *
 * - [QueryParams]의 `toQuery()`에서 principal/path variable을 합성해 완성형으로 생성한다.
 * - QueryParams와 구현을 공유하지 않으며, 응답으로 직렬화되어 나가지 않는다.
 *   (페이지 응답 메타는 PageMeta 스냅샷으로만 노출)
 */
interface Query
