package com.hc.core.domain.entity

import java.time.Instant

interface Auditable {
    val insertedAt: Instant
    val updatedAt: Instant
    val isDeleted: Boolean
    val deletedAt: Instant?
}