package org.whiteprint.platform.core.security.policy

interface SecurityCacheKeyStrategy {

    fun revocationToken(tokenId: String, prefix: String = ""): String {
        val p = prefix.takeIf { it.isNotBlank() }?.let { "$it:" } ?: ""
        return "${p}security:revocation:token:$tokenId"
    }

    fun revocationAccount(subject: String, prefix: String = ""): String {
        val p = prefix.takeIf { it.isNotBlank() }?.let { "$it:" } ?: ""
        return "${p}security:revocation:user:$subject"
    }

    fun forceUpdate(subject: String, prefix: String = ""): String {
        val p = prefix.takeIf { it.isNotBlank() }?.let { "$it:" } ?: ""
        return "${p}security:force-update:$subject"
    }

    fun refreshToken(userId: String, prefix: String = ""): String {
        val p = prefix.takeIf { it.isNotBlank() }?.let { "$it:" } ?: ""
        return "${p}security:refresh-token:$userId"
    }

}