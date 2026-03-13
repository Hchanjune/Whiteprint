package com.hc.core.cache.model

interface DistributedLockOwnerProvider {
    fun provideOwner(): String
}