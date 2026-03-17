package org.whiteprint.platform.core.cache.provider

import org.whiteprint.platform.core.cache.operation.AtomicOperations
import org.whiteprint.platform.core.cache.operation.BatchOperations
import org.whiteprint.platform.core.cache.operation.DistributedLockOperations
import org.whiteprint.platform.core.cache.operation.ListOperations
import org.whiteprint.platform.core.cache.operation.SetOperations
import org.whiteprint.platform.core.cache.operation.ValueOperations

interface CacheProvider {
    val value: org.whiteprint.platform.core.cache.operation.ValueOperations
    val atomic: org.whiteprint.platform.core.cache.operation.AtomicOperations
    val batch: org.whiteprint.platform.core.cache.operation.BatchOperations
    val list: org.whiteprint.platform.core.cache.operation.ListOperations
    val set: org.whiteprint.platform.core.cache.operation.SetOperations
    val lock: org.whiteprint.platform.core.cache.operation.DistributedLockOperations
}