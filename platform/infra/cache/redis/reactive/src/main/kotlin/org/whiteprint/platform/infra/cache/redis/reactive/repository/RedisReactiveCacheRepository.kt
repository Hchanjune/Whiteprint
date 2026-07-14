package org.whiteprint.platform.infra.cache.redis.reactive.repository

import io.github.hchanjune.omk.core.annotations.ManagedCacheRepository
import org.springframework.stereotype.Repository
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import org.whiteprint.platform.core.cache.repository.ReactiveCacheRepository
import java.time.Duration

@Repository
@ManagedCacheRepository
abstract class RedisReactiveCacheRepository(
    override val provider: ReactiveCacheProvider
) : ReactiveCacheRepository {
    abstract override val keyPrefix: String
    abstract override val defaultTtl: Duration
}
