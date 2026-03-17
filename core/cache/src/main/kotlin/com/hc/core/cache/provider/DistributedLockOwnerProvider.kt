package com.hc.core.cache.provider

interface DistributedLockOwnerProvider {
    fun provideOwner(): String
}