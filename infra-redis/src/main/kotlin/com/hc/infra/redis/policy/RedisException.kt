package com.hc.infra.redis.policy

import com.hc.core.exception.StandardException

class RedisException(
    policy: RedisPolicy,
    attributes: Map<String, Any> = emptyMap(),
    cause: Throwable? = null,
): StandardException(policy, attributes, cause)
