package com.hc.core.jpa.entity

import com.hc.core.domain.entity.Auditable

interface AuditableEntity: Auditable {
    fun preUpdate()
    fun delete()
    fun restore()
}