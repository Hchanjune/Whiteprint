package org.whiteprint.platform.infra.cache.redis.reactive.provider

import org.whiteprint.platform.core.cache.operation.ReactiveAtomicOperations
import org.whiteprint.platform.core.cache.operation.ReactiveBatchOperations
import org.whiteprint.platform.core.cache.operation.ReactiveListOperations
import org.whiteprint.platform.core.cache.operation.ReactiveSetOperations
import org.whiteprint.platform.core.cache.operation.ReactiveValueOperations
import org.whiteprint.platform.core.cache.provider.ReactiveCacheProvider

class RedisReactiveCacheProvider(
    override val value: ReactiveValueOperations,
    override val atomic: ReactiveAtomicOperations,
    override val batch: ReactiveBatchOperations,
    override val list: ReactiveListOperations,
    override val set: ReactiveSetOperations,
): ReactiveCacheProvider
