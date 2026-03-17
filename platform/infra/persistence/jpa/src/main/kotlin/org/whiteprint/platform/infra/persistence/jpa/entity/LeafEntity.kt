package org.whiteprint.platform.infra.persistence.jpa.entity

import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class LeafEntity<ID: Serializable, PARENT: RootEntity<out Serializable>>: BaseEntity<ID>() {
    abstract var root: PARENT
}