package com.hc.infra.jpa.core.entity.contract

import com.hc.core.domain.model.contract.Updatable

interface UpdatableEntity: Updatable {
    fun preUpdate()
}