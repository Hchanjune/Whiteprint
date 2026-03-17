package org.whiteprint.platform.infra.persistence.jpa.entity

import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class BranchEntity<ID: Serializable, PARENT: org.whiteprint.platform.infra.persistence.jpa.entity.BaseEntity<out Serializable>>: org.whiteprint.platform.infra.persistence.jpa.entity.RootEntity<ID>() {
    abstract var root: PARENT
}