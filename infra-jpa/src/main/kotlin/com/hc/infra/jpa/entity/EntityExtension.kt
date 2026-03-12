package com.hc.infra.jpa.entity

import com.hc.infra.jpa.exception.EntityIntegrityException
import java.io.Serializable

fun <E: BaseEntity<ID>, ID: Serializable> E.withId(id: ID?): E {
    id?.let { this.assignId(it) }
    return this
}

inline fun <reified E : BaseEntity<*>> E?.ensureExists(rootId: Any): E {
    return this ?: throw EntityIntegrityException(
        targetName = E::class.simpleName ?: "UnknownEntity",
        rootId = rootId.toString()
    )
}