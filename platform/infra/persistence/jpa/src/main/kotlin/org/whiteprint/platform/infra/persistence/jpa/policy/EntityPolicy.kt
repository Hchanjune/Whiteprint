package org.whiteprint.platform.infra.persistence.jpa.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class EntityPolicy(
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