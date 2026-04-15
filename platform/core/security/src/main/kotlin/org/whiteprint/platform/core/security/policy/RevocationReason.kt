package org.whiteprint.platform.core.security.policy

enum class RevocationReason {
    LOGOUT,
    REFRESH_ROTATION,
    FORCE_LOGOUT;

    fun toPolicy(): SecurityPolicy = when (this) {
        LOGOUT, FORCE_LOGOUT -> SecurityPolicy.TOKEN_BLACKLISTED
        REFRESH_ROTATION -> SecurityPolicy.TOKEN_EXPIRED
    }

}