package org.whiteprint.platform.core.domain.model.contract

interface Lockable {
    val lastFencingToken: Long
}