package com.hc.core.domain.contract

import java.time.Instant

interface Deletable {
    val isDeleted: Boolean
    val deletedAt: Instant?
}