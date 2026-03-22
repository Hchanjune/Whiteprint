package org.whiteprint.platform.infra.kms.vault

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.whiteprint.platform.core.kms.model.KeyId
import org.whiteprint.platform.core.kms.model.KeyMaterial
import org.whiteprint.platform.core.kms.model.KeyMetadata
import org.whiteprint.platform.core.kms.service.KeyCache
import java.util.concurrent.TimeUnit

class VaultKeyCache : KeyCache {

    private val metadataCache: Cache<KeyId, KeyMetadata> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(1000)
        .build()

    // 공개키 캐시: 공개키는 거의 변하지 않으므로 더 길게 유지 가능
    private val publicKeyCache: Cache<KeyId, KeyMaterial> = Caffeine.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .maximumSize(500)
        .build()

    override fun getMetadata(keyId: KeyId): KeyMetadata? {
        return metadataCache.getIfPresent(keyId)
    }

    override fun putMetadata(metadata: KeyMetadata) {
        metadataCache.put(metadata.keyId, metadata)
    }

    override fun getPublicKey(keyId: KeyId): KeyMaterial? {
        return publicKeyCache.getIfPresent(keyId)
    }

    override fun putPublicKey(keyId: KeyId, material: KeyMaterial) {
        publicKeyCache.put(keyId, material)
    }

}