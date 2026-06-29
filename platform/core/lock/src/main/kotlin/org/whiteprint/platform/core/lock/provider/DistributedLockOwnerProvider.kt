package org.whiteprint.platform.core.lock.provider

interface DistributedLockOwnerProvider {
    fun provideOwner(): String
}
