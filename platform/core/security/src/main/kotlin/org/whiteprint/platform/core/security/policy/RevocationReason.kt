package org.whiteprint.platform.core.security.policy

enum class RevocationReason {
    LOGOUT,
    FORCE_LOGOUT,
    FORCE_UPDATE,
    ;

    fun toPolicy(): SecurityPolicy = when (this) {
        LOGOUT, FORCE_LOGOUT -> SecurityPolicy.TOKEN_BLACKLISTED
        FORCE_UPDATE -> SecurityPolicy.TOKEN_NEEDS_UPDATE
    }

}