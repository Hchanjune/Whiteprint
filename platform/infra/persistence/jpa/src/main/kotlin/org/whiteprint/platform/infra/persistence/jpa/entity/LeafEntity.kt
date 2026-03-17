package org.whiteprint.platform.infra.persistence.jpa.entity

import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class LeafEntity<ID: Serializable, PARENT: org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity<out Serializable>>: org.whiteprint.platform.infra.persistence.jpa.entity.BaseEntity<ID>() {
    abstract var root: PARENT
}