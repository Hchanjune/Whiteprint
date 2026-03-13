package com.hc.core.cache.provider

import com.hc.core.cache.operation.AtomicOperations
import com.hc.core.cache.operation.BatchOperations
import com.hc.core.cache.operation.DistributedLockOperations
import com.hc.core.cache.operation.ListOperations
import com.hc.core.cache.operation.SetOperations
import com.hc.core.cache.operation.ValueOperations

interface CacheProvider {
    val value: ValueOperations
    val atomic: AtomicOperations
    val batch: BatchOperations
    val list: ListOperations
    val set: SetOperations
    val lock: DistributedLockOperations
}