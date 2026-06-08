package org.whiteprint.sample.auth.adapter.`in`.web.request

data class LogoutRequest(
    val logoutScope: Scope,
    val refreshToken: String?,
) {
    enum class Scope {
        CURRENT_DEVICE,
        ALL_DEVICES;
    }
}