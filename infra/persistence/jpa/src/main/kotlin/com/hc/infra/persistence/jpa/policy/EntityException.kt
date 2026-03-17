package com.hc.infra.persistence.jpa.policy

import com.hc.core.kernel.policy.Policy
import com.hc.core.kernel.policy.exception.StandardException

open class EntityException(
    policy: Policy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)