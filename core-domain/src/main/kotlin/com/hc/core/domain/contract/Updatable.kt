package com.hc.core.domain.contract

import java.time.Instant

interface Updatable {
    val updatedAt: Instant
    val version: Long
}