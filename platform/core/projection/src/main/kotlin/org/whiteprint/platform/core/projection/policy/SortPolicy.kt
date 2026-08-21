package org.whiteprint.platform.core.projection.policy

import org.whiteprint.platform.core.kernel.policy.Policy

enum class SortPolicy (
    override val status: Int,
    override val code: String,
    override val message: String
): Policy {

    /**
     * Required Attributes
     * - [keys]
     */
    SORT_FIELD_MAP_FAILED(
        status = 400,
        code = "SORT_FIELD_MAP_FAILED",
        message = "Available sort fields : [[keys]]"
    )

}