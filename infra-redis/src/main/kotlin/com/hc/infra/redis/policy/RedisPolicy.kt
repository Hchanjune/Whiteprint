package com.hc.infra.redis.policy

import com.hc.core.exception.Policy

enum class RedisPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    REQUIRED_KEY_NOT_FOUND(404, "REDIS_REQUIRED_KEY_NOT_FOUND", "Required cache data for [[key]] is missing"),
    /**
     * Require Stacktrace
     */
    INFRA_FAILURE(500, "REDIS_INFRA_ERROR", "Redis infrastructure error occurred.")

}