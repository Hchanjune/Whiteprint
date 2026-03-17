package org.whiteprint.platform.infra.cache.redis.repository

import com.hc.core.cache.provider.CacheProvider
import com.hc.core.cache.repository.CacheRepository
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
abstract class RedisCacheRepository(
    override val provider: CacheProvider
) : CacheRepository {
    abstract override val keyPrefix: String
    abstract override val defaultTtl: Duration
}