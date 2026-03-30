package org.whiteprint.platform.core.domain.model.contract

import java.time.Instant

interface Deletable {
    val isDeleted: Boolean
    val deletedAt: Instant?
}