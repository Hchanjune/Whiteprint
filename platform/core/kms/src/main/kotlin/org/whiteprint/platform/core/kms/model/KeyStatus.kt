package org.whiteprint.platform.core.kms.model

enum class KeyStatus {
    /** 활성화 상태: 암호화/복호화/서명/검증 모두 가능 */
    ENABLED,

    /** 비활성화 상태: 관리자에 의해 일시 정지됨. 모든 연산 불가 */
    DISABLED,

    /** 만료됨: 유효 기간이 지남. 검증/복호화만 허용할지 여부는 정책에 따름 */
    EXPIRED,

    /** 아카이브: 더 이상 사용되지 않는 과거 버전. 검증/복호화용으로만 유지 */
    ARCHIVED,

    /** 삭제 대기: 삭제 명령 후 유예 기간(Grace Period) 상태 */
    PENDING_DELETION,

    /** 손상됨: 키 유출이 의심되어 강제 차단된 상태  */
    COMPROMISED
}