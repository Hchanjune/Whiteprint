package org.whiteprint.platform.core.persistence.model.contract

interface Lockable {
    val lastFencingToken: String?
}