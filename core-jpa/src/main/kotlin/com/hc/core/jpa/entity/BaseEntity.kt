package com.hc.core.jpa.entity

import com.hc.core.jpa.entity.contract.PersistableEntity
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import java.io.Serializable

@MappedSuperclass
abstract class BaseEntity<ID: Serializable>: PersistableEntity<ID> {

    @Id
    @Column(name = "id")
    private var _id: ID? = null

    @get: Transient
    override val id: ID
        get() = _id?: throw IllegalArgumentException("Entity ID has not been Initialized")

    @Transient
    private var _isNew: Boolean = true

    override val isNew: Boolean
        get() = _isNew

    override fun assignId(id: ID) {
        this._id = id
        this._isNew = false
    }

    @PostPersist
    @PostLoad
    protected fun markNotNew() {
        this._isNew = false
    }

}