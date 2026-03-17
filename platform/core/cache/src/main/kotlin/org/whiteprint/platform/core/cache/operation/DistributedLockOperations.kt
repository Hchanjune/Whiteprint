package org.whiteprint.platform.core.cache.operation

import org.whiteprint.platform.core.cache.model.CacheKey
import org.whiteprint.platform.core.cache.model.CacheLockHandle
import java.time.Duration

interface DistributedLockOperations {

    fun acquireLock(
        key: org.whiteprint.platform.core.cache.model.CacheKey,
        ttl: Duration
    ): org.whiteprint.platform.core.cache.model.CacheLockHandle?

    fun releaseLock(lock: org.whiteprint.platform.core.cache.model.CacheLockHandle): Boolean

    fun extendLock(lock: org.whiteprint.platform.core.cache.model.CacheLockHandle, ttl: Duration): Boolean

}