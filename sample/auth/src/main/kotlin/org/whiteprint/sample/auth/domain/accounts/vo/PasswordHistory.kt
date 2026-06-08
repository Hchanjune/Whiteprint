package org.whiteprint.sample.auth.domain.accounts.vo

import org.springframework.security.crypto.password.PasswordEncoder

data class PasswordHistory(
    val hashes: List<PasswordHash> = emptyList(),
    val maxHistorySize: Int = 5
) {

    fun update(newHash: PasswordHash): PasswordHistory {
        val updatedHashes = (listOf(newHash) + hashes).take(maxHistorySize)
        return this.copy(hashes = updatedHashes)
    }

    fun isPreviouslyUsed(newHash: PasswordHash, encoder: PasswordEncoder): Boolean {
        return hashes.any { it.value == newHash.value }
    }

}
