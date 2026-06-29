package org.whiteprint.platform.core.projection.model

import java.io.Serializable
import java.time.Instant

abstract class ProjectionModel<ID : Serializable> {
    abstract val id: ID
    abstract val version: Long
    abstract val insertedAt: Instant
    abstract val updatedAt: Instant
    abstract val useSoftDelete: Boolean
    abstract val isDeleted: Boolean
    abstract val deletedAt: Instant?
}
