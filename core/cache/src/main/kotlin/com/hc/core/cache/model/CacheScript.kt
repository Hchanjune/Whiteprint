package com.hc.core.cache.model

interface CacheScript<T> {
    val script: String
    val resultType: Class<T>
}