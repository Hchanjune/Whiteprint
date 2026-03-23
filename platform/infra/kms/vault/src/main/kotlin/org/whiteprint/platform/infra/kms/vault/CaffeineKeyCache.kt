package org.whiteprint.platform.infra.kms.vault

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.whiteprint.platform.core.kms.model.KeyBundle
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeySide
import org.whiteprint.platform.core.kms.service.KeyCache
import java.util.concurrent.TimeUnit

class CaffeineKeyCache(
    expiresAfterWriteMinutes: Long = 60,
    maximumSize: Long = 1000
) : KeyCache {

    private data class CacheKey(
        val keyId: KeyId,
        val side: KeySide,
    )

    private val bundleCache: Cache<CacheKey, KeyBundle> = Caffeine.newBuilder()
        .expireAfterWrite(expiresAfterWriteMinutes, TimeUnit.MINUTES)
        .maximumSize(maximumSize)
        .build()

    override fun getBundle(
        keyId: KeyId,
        side: KeySide
    ): KeyBundle? {
        return bundleCache.getIfPresent(CacheKey(keyId, side))
    }

    override fun putBundle(
        side: KeySide,
        bundle: KeyBundle
    ) {
        bundleCache.put(CacheKey(bundle.metadata.keyId, side), bundle)
    }

    override fun evict(keyId: KeyId) {
        val keysToRemove = bundleCache.asMap().keys.filter { it.keyId.alias == keyId.alias }
        bundleCache.invalidateAll(keysToRemove)
    }

    override fun clear() {
        bundleCache.invalidateAll()
    }


}