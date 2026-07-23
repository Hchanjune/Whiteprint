package org.whiteprint.platform.core.projection.model.readModel

/**
 * 애플리케이션 레이어(query service)가 반환하는 읽기 모델 계약.
 *
 * - 단일 [org.whiteprint.platform.core.projection.model.projection.Projection] 또는
 *   여러 Projection을 묶은 조합 모델이 이 마커를 구현한다.
 * - adapter 타입([org.whiteprint.platform.core.projection.model.viewModel.ViewModel])을
 *   port 반환 타입으로 쓰지 않기 위한 경계 표식이다. ViewModel 변환은 웹 어댑터 매퍼의 책임.
 */
interface ReadModel
