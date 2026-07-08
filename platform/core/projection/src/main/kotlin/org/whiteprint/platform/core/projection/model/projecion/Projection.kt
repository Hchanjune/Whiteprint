package org.whiteprint.platform.core.projection.model.projecion

import java.time.Instant

interface Projection {
    val id: String
    val version: Long
    val insertedAt: Instant
    val updatedAt: Instant
    val useSoftDelete: Boolean
    val isDeleted: Boolean
    val deletedAt: Instant?
}