package org.whiteprint.platform.core.projection.model.viewModel

/**
 * 웹 어댑터(adapter.in.web)의 최종 응답 모델 계약. 클라이언트에게 나가는 형태는 항상 ViewModel이다.
 *
 * - [org.whiteprint.platform.core.projection.model.readModel.ReadModel]을 웹 매퍼에서 변환해 만든다.
 * - adapter 밖(application/port)에서 선언하거나 반환하지 않는다.
 */
interface ViewModel
