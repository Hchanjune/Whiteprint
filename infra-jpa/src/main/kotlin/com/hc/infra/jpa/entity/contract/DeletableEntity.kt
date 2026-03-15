package com.hc.infra.jpa.entity.contract

import com.hc.core.domain.model.contract.Deletable

interface DeletableEntity: Deletable {
    val useSoftDelete: Boolean
    fun delete()
    fun restore()
}