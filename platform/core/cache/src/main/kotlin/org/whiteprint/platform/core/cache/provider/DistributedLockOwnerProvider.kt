package org.whiteprint.platform.core.cache.provider

interface DistributedLockOwnerProvider {
    fun provideOwner(): String
}