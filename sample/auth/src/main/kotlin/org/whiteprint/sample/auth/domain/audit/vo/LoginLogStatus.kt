package org.whiteprint.sample.auth.domain.audit.vo

enum class LoginLogStatus(
    val value: String
) {
    SUCCESS("성공"),
    FAIL("실패"),
}