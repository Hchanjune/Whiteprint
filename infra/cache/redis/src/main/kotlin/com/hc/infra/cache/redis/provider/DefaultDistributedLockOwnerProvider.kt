package com.hc.infra.cache.redis.provider

import com.hc.core.cache.provider.DistributedLockOwnerProvider
import java.net.InetAddress
import java.util.UUID

class DefaultDistributedLockOwnerProvider : DistributedLockOwnerProvider {

    companion object {
        private val OWNER_ID = try {
            "${InetAddress.getLocalHost().hostName}:${UUID.randomUUID().toString().take(8)}"
        } catch (exception: Exception) {
            UUID.randomUUID().toString()
        }

        fun currentOwner(): String = OWNER_ID
    }

    override fun provideOwner(): String = OWNER_ID
}