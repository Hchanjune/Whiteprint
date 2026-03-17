package org.whiteprint.platform.infra.persistence.jpa.entity

import org.whiteprint.platform.infra.persistence.jpa.policy.EntityException
import org.whiteprint.platform.infra.persistence.jpa.policy.EntityPolicy
import java.io.Serializable

fun <E: BaseEntity<ID>, ID: Serializable> E.withId(id: ID?): E {
    id?.let { this.assignId(it) }
    return this
}

inline fun <reified E : BaseEntity<*>> E?.ensureExistsOrThrow(rootId: Any): E {
    return this ?:
        throw EntityException(
            policy = EntityPolicy.INTEGRITY_VIOLATION,
            attributes = mapOf(
                "targetName" to (E::class.simpleName ?: "UnknownEntity"),
                "rootId" to rootId.toString()
            )
        )
}