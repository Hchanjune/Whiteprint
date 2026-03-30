package org.whiteprint.platform.core.persistence.model.contract

import java.time.Instant

interface Updatable {
    val updatedAt: Instant
    val version: Long
}