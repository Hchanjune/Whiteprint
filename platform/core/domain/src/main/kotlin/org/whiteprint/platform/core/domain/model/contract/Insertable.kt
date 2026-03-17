package org.whiteprint.platform.core.domain.model.contract

import java.time.Instant

interface Insertable {
    val insertedAt: Instant
}