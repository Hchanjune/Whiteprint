package org.whiteprint.platform.core.lock.model

data class LockHandle(
    val key: LockKey,
    val owner: String,
    val fencingToken: Long
)
