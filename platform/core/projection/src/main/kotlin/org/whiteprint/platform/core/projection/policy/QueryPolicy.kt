package org.whiteprint.platform.core.projection.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class QueryPolicy(
    override val status: Int,
    override val code: String,
    override val message: String,
) : Policy {

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    QUERY_NOT_FOUND(
        404,
        "QUERY_NOT_FOUND",
        "Query result not found. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    PROJECTION_NOT_FOUND(
        404,
        "PROJECTION_NOT_FOUND",
        "Projection not found. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    PROJECTION_NOT_READY(
        503,
        "PROJECTION_NOT_READY",
        "Projection is not ready yet. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    PROJECTION_STALE(
        409,
        "PROJECTION_STALE",
        "Projection is stale. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    INVALID_QUERY(
        400,
        "QUERY_INVALID",
        "Invalid query. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    INVALID_SORT_FIELD(
        400,
        "QUERY_INVALID_SORT_FIELD",
        "Invalid sort field. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    INVALID_FILTER(
        400,
        "QUERY_INVALID_FILTER",
        "Invalid filter. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    INVALID_PAGE(
        400,
        "QUERY_INVALID_PAGE",
        "Invalid page number. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    INVALID_PAGE_SIZE(
        400,
        "QUERY_INVALID_PAGE_SIZE",
        "Invalid page size. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    INVALID_CURSOR(
        400,
        "QUERY_INVALID_CURSOR",
        "Invalid cursor. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    QUERY_FORBIDDEN(
        403,
        "QUERY_FORBIDDEN",
        "You do not have permission to execute this query. ([[key]]=[[value]])"
    ),

    /**
     * RequiredAttributes
     * - [key]
     * - [value]
     */
    QUERY_TIMEOUT(
        504,
        "QUERY_TIMEOUT",
        "Query execution timed out. ([[key]]=[[value]])"
    ),

}