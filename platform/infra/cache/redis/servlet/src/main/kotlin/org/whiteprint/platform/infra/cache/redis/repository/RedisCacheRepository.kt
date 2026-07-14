package org.whiteprint.platform.infra.cache.redis.repository

import io.github.hchanjune.omk.core.annotations.ManagedCacheRepository
import org.springframework.stereotype.Repository
import org.whiteprint.platform.core.cache.provider.CacheProvider
import org.whiteprint.platform.core.cache.repository.CacheRepository
import java.time.Duration

@Repository
@ManagedCacheRepository
abstract class RedisCacheRepository(
    override val provider: CacheProvider
) : CacheRepository {
    abstract override val keyPrefix: String
    abstract override val defaultTtl: Duration
}