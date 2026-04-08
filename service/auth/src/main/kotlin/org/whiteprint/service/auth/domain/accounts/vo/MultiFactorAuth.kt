package org.whiteprint.service.auth.domain.accounts.vo

data class MultiFactorAuth(
    val secret: String?,
) {
    val isEnabled: Boolean = !secret.isNullOrBlank()

    fun verify(code: String): Boolean {
        if (!isEnabled) return false
        return true
    }
}
