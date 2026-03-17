package com.hc.infra.persistence.jpa.policy

import com.hc.core.kernel.policy.Policy

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