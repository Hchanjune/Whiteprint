package com.hc.infra.jpa.core.entity

import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class LeafEntity<ID: Serializable, PARENT: com.hc.infra.jpa.core.entity.RootEntity<out Serializable>>: com.hc.infra.jpa.core.entity.BaseEntity<ID>() {
    abstract var root: PARENT
}