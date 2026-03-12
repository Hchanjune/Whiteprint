package com.hc.infra.jpa.policy

import com.hc.core.exception.Policy

enum class EntityPolicy(
    override val status: Int,
    override val code: String,
    override val message: String
): Policy {
    /**
     * Require Attributes
     * - [targetName]
     * - [rootId]
     */
    INTEGRITY_VIOLATION(400, "INTEGRITY_VIOLATION", "[targetName] not found for [rootId]"),
}