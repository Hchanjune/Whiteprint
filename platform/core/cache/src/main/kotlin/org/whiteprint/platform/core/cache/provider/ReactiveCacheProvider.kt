package org.whiteprint.platform.core.cache.provider

import org.whiteprint.platform.core.cache.operation.ReactiveAtomicOperations
import org.whiteprint.platform.core.cache.operation.ReactiveBatchOperations
import org.whiteprint.platform.core.cache.operation.ReactiveListOperations
import org.whiteprint.platform.core.cache.operation.ReactiveSetOperations
import org.whiteprint.platform.core.cache.operation.ReactiveValueOperations

interface ReactiveCacheProvider {
    val value: ReactiveValueOperations
    val atomic: ReactiveAtomicOperations
    val batch: ReactiveBatchOperations
    val list: ReactiveListOperations
    val set: ReactiveSetOperations
}
