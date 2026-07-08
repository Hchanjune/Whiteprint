package org.whiteprint.platform.core.cache.repository

import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider
import java.time.Duration

interface ReactiveCacheRepository {
    val keyPrefix: String
    val defaultTtl: Duration
    val provider: ReactiveCacheProvider
}
