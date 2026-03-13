package com.hc.core.cache.repository

import com.hc.core.cache.provider.CacheProvider
import java.time.Duration

interface CacheRepository {
    val keyPrefix: String
    val defaultTtl: Duration
    val provider: CacheProvider
}