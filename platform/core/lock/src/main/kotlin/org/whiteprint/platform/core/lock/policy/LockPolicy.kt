package org.whiteprint.platform.core.lock.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class LockPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    /**
     * RequiredAttributes
     * - [key]
     */
    ACQUISITION_FAILED(409, "LOCK_ACQUISITION_FAILED", "Failed to acquire distributed lock. key:[[key]]"),

    /**
     * RequiredAttributes
     * - [key]
     */
    NO_LOCK_KEY_DEFINED(500, "LOCK_NO_KEY_DEFINED", "No @DistributedLockKey found on method or its parameters. method:[[key]]"),

}
