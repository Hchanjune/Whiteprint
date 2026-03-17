package com.hc.core.messaging.policy

import com.hc.core.kernel.policy.exception.StandardException

class EventException(
    policy: EventPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)