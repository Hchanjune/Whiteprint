package org.whiteprint.platform.core.cache.policy

import org.whiteprint.platform.core.kernel.policy.exception.StandardException

class CacheException(
    policy: org.whiteprint.platform.core.cache.policy.CachePolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)