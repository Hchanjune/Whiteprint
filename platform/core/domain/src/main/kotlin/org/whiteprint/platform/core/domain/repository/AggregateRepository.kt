package org.whiteprint.platform.core.domain.repository

import org.whiteprint.platform.core.domain.model.aggregate.Aggregate
import java.io.Serializable

interface AggregateRepository<ID: Serializable, T: Aggregate<*>> {

    fun findByIdOrThrow(id: ID): T

    fun findByIdOrNull(id: ID): T?

    fun create(aggregate: T): T

    fun update(aggregate: T): T

    fun delete(aggregate: T)

    fun restore(aggregate: T): T

    fun updateAll(aggregates: Collection<T>): List<T>

    fun deleteAll(aggregates: Collection<T>)

    fun restoreAll(aggregates: Collection<T>): List<T>

}