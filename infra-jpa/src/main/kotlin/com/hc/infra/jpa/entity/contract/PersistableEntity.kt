package com.hc.infra.jpa.entity.contract

import com.hc.core.domain.contract.Identifiable
import java.io.Serializable

interface PersistableEntity<ID: Serializable>: Identifiable<ID> {
    val isNew: Boolean
    fun assignId(id: ID)
}