package com.hc.core.domain.model.contract

import java.time.Instant

interface Insertable {
    val insertedAt: Instant
}