package com.hc.core.domain.contract

import java.time.Instant

interface Insertable {
    val insertedAt: Instant
}