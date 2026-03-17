package org.whiteprint.platform.infra.cache.redis.provider

import org.whiteprint.platform.core.cache.operation.AtomicOperations
import org.whiteprint.platform.core.cache.operation.BatchOperations
import org.whiteprint.platform.core.cache.operation.DistributedLockOperations
import org.whiteprint.platform.core.cache.operation.ListOperations
import org.whiteprint.platform.core.cache.operation.SetOperations
import org.whiteprint.platform.core.cache.operation.ValueOperations
import org.whiteprint.platform.core.cache.provider.CacheProvider

class RedisCacheProvider(
    override val value: ValueOperations,
    override val atomic: AtomicOperations,
    override val batch: BatchOperations,
    override val list: ListOperations,
    override val set: SetOperations,
    override val lock: DistributedLockOperations
): CacheProvider