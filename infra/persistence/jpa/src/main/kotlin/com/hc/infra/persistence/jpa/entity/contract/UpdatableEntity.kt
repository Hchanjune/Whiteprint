package com.hc.infra.persistence.jpa.entity.contract

import com.hc.core.domain.model.contract.Updatable

interface UpdatableEntity: Updatable {
    fun preUpdate()
}