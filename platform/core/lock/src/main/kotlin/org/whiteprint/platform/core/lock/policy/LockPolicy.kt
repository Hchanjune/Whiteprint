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

    /**
     * `@FencingGuarded` 엔티티 갱신 거절 — 현재 락의 토큰이 행에 마지막으로 기록된 토큰보다 오래됐다.
     * 락을 잃은 옛 보유자가 새 보유자가 쓴 행을 덮어쓰려 한 것이다. 재시도하면 새 락·새 토큰으로 다시 판단된다.
     *
     * RequiredAttributes
     * - [entity]
     * - [id]
     * - [token]
     * - [lastToken]
     */
    FENCING_TOKEN_REJECTED(409, "LOCK_FENCING_TOKEN_REJECTED", "Write rejected by fencing token. entity:[[entity]] id:[[id]] token:[[token]] lastToken:[[lastToken]]"),

}
