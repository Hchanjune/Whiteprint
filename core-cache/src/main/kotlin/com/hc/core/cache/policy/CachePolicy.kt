package com.hc.core.cache.policy

import com.hc.core.exception.Policy

enum class CachePolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
): Policy {

    /**
     * RequiredAttributes
     * - [key]
     * - [delta]
     */
    INCREMENT_FAILED(500, "CACHE_INCREMENT_FAILED", "Cache increment failed. key:[[key]] inputDelta:[[delta]]"),

    /**
     * RequiredAttributes
     * - [key]
     * - [delta]
     */
    DECREMENT_FAILED(500, "CACHE_DECREMENT_FAILED", "Cache decrement failed. key:[[key]] inputDelta:[[delta]]"),

    /**
     * RequiredAttributes
     * - [key]
     * - [current]
     * - [delta]
     * - [limit]
     */
    INCREMENT_LIMIT_EXCEEDED(400, "CACHE_INCREMENT_LIMIT_EXCEEDED", "Cache increment limit exceeded key:[[key]] current:[[current]] delta:[[delta]] limit[[limit]]"),

    /**
     * RequiredAttributes
     * - [key]
     * - [current]
     * - [delta]
     * - [limit]
     */
    DECREMENT_LIMIT_EXCEEDED(400, "CACHE_DECREMENT_LIMIT_EXCEEDED", "Cache decrement limit exceeded key:[[key]] current:[[current]] delta:[[delta]] limit[[limit]]"),

    /**
     * RequiredAttributes
     * - [ttl]
     */
    TTL_MUST_BE_POSITIVE(400, "CACHE_TTL_MUST_BE_POSITIVE", "TTL must be POSITIVE key:[[key]] input:[[ttl]]"),

    /**
     * RequiredAttributes
     * - [key]
     * - [expectedType]
     * - [actualType]
     */
    CLASS_CAST_FAILED(500, "CACHE_CLASS_CAST_FAILED", "Class cast failed. key:[[key]] expectedType:[[expectedType]] but actualType:[[actualType]]"),

    REQUIRED_KEY_NOT_FOUND(404, "CACHE_REQUIRED_KEY_NOT_FOUND", "Required cache data for [[key]] is missing"),

    /**
     * Require Stacktrace
     *
     * RequiredAttributes
     * - [requestedKey]
     * - [reason]?
     */
    INFRA_FAILURE(500, "CACHE_INFRA_ERROR", "Unknown Cache infrastructure error occurred. requestedKey:[[requestedKey]] reason:[[reason]]"),

}