package org.whiteprint.platform.infra.persistence.jpa.entity.contract

import org.whiteprint.platform.core.domain.model.contract.Identifiable
import java.io.Serializable

interface PersistableEntity<ID: Serializable>: Identifiable<ID> {
    val isNew: Boolean
    fun assignId(id: ID)
}