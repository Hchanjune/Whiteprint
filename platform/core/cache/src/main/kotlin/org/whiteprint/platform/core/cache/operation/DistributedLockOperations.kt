package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.model.CacheLockHandle
import java.time.Duration

interface DistributedLockOperations {

    fun acquireLock(
        key: CacheKey,
        ttl: Duration
    ): CacheLockHandle?

    fun releaseLock(lock: CacheLockHandle): Boolean

    fun extendLock(lock: CacheLockHandle, ttl: Duration): Boolean

}