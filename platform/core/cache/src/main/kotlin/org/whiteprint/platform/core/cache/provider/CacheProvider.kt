package org.whiteprint.platform.core.cache.provider

import org.whiteprint.platform.core.cache.operation.AtomicOperations
import org.whiteprint.platform.core.cache.operation.BatchOperations
import org.whiteprint.platform.core.cache.operation.DistributedLockOperations
import org.whiteprint.platform.core.cache.operation.ListOperations
import org.whiteprint.platform.core.cache.operation.SetOperations
import org.whiteprint.platform.core.cache.operation.ValueOperations

interface CacheProvider {
    val value: ValueOperations
    val atomic: AtomicOperations
    val batch: BatchOperations
    val list: ListOperations
    val set: SetOperations
    val lock: DistributedLockOperations
}