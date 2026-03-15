package com.hc.core.cache.policy

import com.hc.core.kernel.policy.exception.StandardException

class CacheException(
    policy: CachePolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)