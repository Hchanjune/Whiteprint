package com.hc.infra.jpa.entity.contract

import com.hc.core.domain.contract.Updatable

interface UpdatableEntity: Updatable {
    fun preUpdate()
}