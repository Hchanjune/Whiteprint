package com.hc.infra.jpa.core.entity

import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class BranchEntity<ID: Serializable, PARENT: com.hc.infra.jpa.core.entity.BaseEntity<out Serializable>>: com.hc.infra.jpa.core.entity.RootEntity<ID>() {
    abstract var root: PARENT
}