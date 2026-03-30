package org.whiteprint.platform.core.domain.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class PersistencePolicy(
    override val status: Int,
    override val code: String,
    override val message: String
): Policy {
    /**
     * RequireAttributes
     * - [targetName]
     * - [rootId]
     */
    INTEGRITY_VIOLATION(400, "INTEGRITY_VIOLATION", "[[targetName]] not found for [[rootId]]"),
}