package org.whiteprint.platform.infra.persistence.jpa.entity.contract

import org.whiteprint.platform.core.domain.model.contract.Updatable

interface UpdatableEntity: Updatable {
    fun preUpdate()
}