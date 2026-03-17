package com.hc.infra.persistence.jpa.entity.contract

import com.hc.core.domain.model.contract.Identifiable
import java.io.Serializable

interface PersistableEntity<ID: Serializable>: Identifiable<ID> {
    val isNew: Boolean
    fun assignId(id: ID)
}