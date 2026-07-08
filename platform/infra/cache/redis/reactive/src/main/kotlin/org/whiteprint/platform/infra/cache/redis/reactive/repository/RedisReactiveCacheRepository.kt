package org.whiteprint.platform.infra.cache.redis.reactive.repository

import org.springframework.stereotype.Repository
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import org.whiteprint.platform.core.cache.repository.ReactiveCacheRepository
import java.time.Duration

@Repository
abstract class RedisReactiveCacheRepository(
    override val provider: ReactiveCacheProvider
) : ReactiveCacheRepository {
    abstract override val keyPrefix: String
    abstract override val defaultTtl: Duration
}
