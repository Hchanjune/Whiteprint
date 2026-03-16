package com.hc.infra.jpa.core.entity

import com.hc.infra.jpa.core.policy.EntityException
import com.hc.infra.jpa.core.policy.EntityPolicy
import java.io.Serializable

fun <E: com.hc.infra.jpa.core.entity.BaseEntity<ID>, ID: Serializable> E.withId(id: ID?): E {
    id?.let { this.assignId(it) }
    return this
}

inline fun <reified E : com.hc.infra.jpa.core.entity.BaseEntity<*>> E?.ensureExistsOrThrow(rootId: Any): E {
    return this ?:
        throw _root_ide_package_.com.hc.infra.jpa.core.policy.EntityException(
            policy = _root_ide_package_.com.hc.infra.jpa.core.policy.EntityPolicy.INTEGRITY_VIOLATION,
            attributes = mapOf(
                "targetName" to (E::class.simpleName ?: "UnknownEntity"),
                "rootId" to rootId.toString()
            )
        )
}