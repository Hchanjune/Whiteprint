package com.hc.infra.cache.redis.provider

import com.hc.core.cache.operation.AtomicOperations
import com.hc.core.cache.operation.BatchOperations
import com.hc.core.cache.operation.DistributedLockOperations
import com.hc.core.cache.operation.ListOperations
import com.hc.core.cache.operation.SetOperations
import com.hc.core.cache.operation.ValueOperations
import com.hc.core.cache.provider.CacheProvider

class RedisCacheProvider(
    override val value: ValueOperations,
    override val atomic: AtomicOperations,
    override val batch: BatchOperations,
    override val list: ListOperations,
    override val set: SetOperations,
    override val lock: DistributedLockOperations
): CacheProvider