package org.whiteprint.platform.core.projection.policy

import org.whiteprint.platform.core.kernel.policy.exception.StandardException

class QueryException(
    policy: QueryPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)