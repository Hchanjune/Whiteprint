package com.hc.infra.redis.policy

import com.hc.core.exception.Policy

enum class RedisPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    /**
     * RequiredAttributes
     * - [ttl]
     */
    TTL_MUST_BE_POSITIVE(400, "REDIS_TTL_MUST_BE_POSITIVE", "TTL must be POSITIVE input: [[ttl]]"),

    /**
     * RequiredAttributes
     * - [expectedType]
     * - [actualType]
     */
    CLASS_CAST_FAILED(500, "REDIS_CLASS_CAST_FAILED", "Class cast failed: expectedType:[[expectedType]] but actualType:[[actualType]]"),

    REQUIRED_KEY_NOT_FOUND(404, "REDIS_REQUIRED_KEY_NOT_FOUND", "Required cache data for [[key]] is missing"),
    /**
     * Require Stacktrace
     */
    INFRA_FAILURE(500, "REDIS_INFRA_ERROR", "Redis infrastructure error occurred.")

}