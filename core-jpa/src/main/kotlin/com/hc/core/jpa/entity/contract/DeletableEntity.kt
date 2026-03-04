package com.hc.core.jpa.entity.contract

import com.hc.core.domain.contract.Deletable

interface DeletableEntity: Deletable {
    val useSoftDelete: Boolean
    fun delete()
    fun restore()
}