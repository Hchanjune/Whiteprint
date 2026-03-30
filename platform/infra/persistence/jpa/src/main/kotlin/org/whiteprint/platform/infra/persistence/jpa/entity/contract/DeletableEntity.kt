package org.whiteprint.platform.infra.persistence.jpa.entity.contract

import org.whiteprint.platform.core.persistence.model.contract.Deletable

interface DeletableEntity: Deletable {
    val useSoftDelete: Boolean
    fun delete()
    fun restore()
}