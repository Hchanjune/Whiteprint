package com.hc.core.cache.operation

import com.hc.core.cache.model.CacheKey
import com.hc.core.cache.model.CacheLockHandle
import java.time.Duration

interface DistributedLockOperations {

    fun acquireLock(
        key: CacheKey,
        ttl: Duration
    ): CacheLockHandle?

    fun releaseLock(lock: CacheLockHandle): Boolean

    fun extendLock(lock: CacheLockHandle, ttl: Duration): Boolean

}