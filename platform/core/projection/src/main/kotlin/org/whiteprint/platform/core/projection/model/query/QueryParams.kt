package org.whiteprint.platform.core.projection.model.query

/**
 * 웹 어댑터(adapter.in.web)의 조회 요청 바인딩 모델 계약.
 *
 * - 모든 조회 엔드포인트는 QueryParams 하나를 받는다. 파라미터가 없으면 빈 객체라도 선언한다.
 *   (command 사이드의 Request와 대칭)
 * - `toQuery(principal, pathVariable...)` 매퍼를 통해서만 [Query]로 변환되며,
 *   adapter 밖(service 시그니처)으로 직접 전달하지 않는다.
 */
interface QueryParams
