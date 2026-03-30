package org.whiteprint.platform.core.persistence.model.aggregate

import org.whiteprint.platform.core.persistence.model.contract.Auditable
import org.whiteprint.platform.core.persistence.model.contract.Identifiable
import org.whiteprint.platform.core.persistence.model.contract.LifeCycle
import java.io.Serializable

abstract class Aggregate<ROOT: Any>:
    Identifiable<Serializable>,
    Auditable,
    LifeCycle {
    abstract val schemaVersion: String
    abstract override val id: Serializable
    abstract val root: ROOT
    val aggregateType: String by lazy { root::class.simpleName ?: "UNKNOWN" }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aggregate<*>) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

}