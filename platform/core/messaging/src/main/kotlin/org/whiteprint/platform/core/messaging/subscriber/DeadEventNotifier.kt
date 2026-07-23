package org.whiteprint.platform.core.messaging.subscriber

import org.whiteprint.platform.core.messaging.inbox.EventInbox

/**
 * 인박스 이벤트가 DEAD(재시도 소진 종결)로 전환될 때 호출되는 서비스 전역 알림 포트.
 *
 * 서비스가 이 인터페이스의 빈을 하나 등록하면 **모든 인박스 핸들러**의 DEAD 이벤트에
 * 공통으로 적용된다(슬랙/메트릭/페이저 등). 핸들러별 추가 동작이 필요하면
 * AbstractEventHandler.onEventDead 를 개별 override 하면 되고, 둘은 함께 동작한다
 * (전역 노티파이어 → 핸들러 훅 순, 각각의 예외는 삼켜진다).
 *
 * PARTITION_ORDERED 모드에선 DEAD 가 해당 partition_key 를 블로킹하므로
 * 운영 대응(수동 복구: status='RECEIVED', attempt_count=0)이 필요하다.
 */
fun interface DeadEventNotifier {
    fun notifyDead(record: EventInbox, exception: Exception)
}
