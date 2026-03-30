package org.whiteprint.platform.core.persistence.model.contract

import java.time.Instant

interface Insertable {
    val insertedAt: Instant
}