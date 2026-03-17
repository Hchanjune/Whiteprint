package org.whiteprint.platform.core.cache.repository

import org.whiteprint.platform.core.cache.provider.CacheProvider
import java.time.Duration

interface CacheRepository {
    val keyPrefix: String
    val defaultTtl: Duration
    val provider: org.whiteprint.platform.core.cache.provider.CacheProvider
}