package com.hc.infra.persistence.jpa.entity

import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class BranchEntity<ID: Serializable, PARENT: BaseEntity<out Serializable>>: RootEntity<ID>() {
    abstract var root: PARENT
}