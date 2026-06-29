package org.whiteprint.platform.core.lock.operation

import org.whiteprint.platform.core.lock.model.LockHandle
import org.whiteprint.platform.core.lock.model.LockKey
import java.time.Duration

interface DistributedLockOperations {

    fun acquireLock(
        key: LockKey,
        ttl: Duration
    ): LockHandle?

    fun releaseLock(lock: LockHandle): Boolean

    fun extendLock(lock: LockHandle, ttl: Duration): Boolean

}
