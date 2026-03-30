package org.whiteprint.platform.core.persistence.model.contract

import java.time.Instant

interface Deletable {
    val isDeleted: Boolean
    val deletedAt: Instant?
}