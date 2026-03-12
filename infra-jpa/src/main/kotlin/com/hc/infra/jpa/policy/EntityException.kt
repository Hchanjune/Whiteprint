package com.hc.infra.jpa.policy

import com.hc.core.exception.Policy
import com.hc.core.exception.StandardException

open class EntityException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)