package org.whiteprint.platform.core.security.verifier

interface AccountTokenStatusManager {
    fun setForceUpdate(subject: String, updatedAt: Long)
    fun checkForceUpdate(subject: String, issuedAt: Long): Boolean
}