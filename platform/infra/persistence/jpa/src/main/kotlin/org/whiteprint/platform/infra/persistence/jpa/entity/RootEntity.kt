package org.whiteprint.platform.infra.persistence.jpa.entity

import org.whiteprint.platform.infra.persistence.jpa.entity.contract.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import jakarta.persistence.Version
import org.whiteprint.platform.infra.persistence.jpa.entity.contract.LockableEntity
import java.io.Serializable
import java.time.Instant

@MappedSuperclass
abstract class RootEntity<ID: Serializable>: BaseEntity<ID>(), AuditableEntity, LockableEntity {

    // 감사 필드 표준 순서: version, insertedAt, updatedAt, isDeleted, deletedAt (Auditable 계약과 동일)
    @Version
    @Column(name = "version", nullable = false)
    override var version: Long = 0
        protected set
    @Column(name = "inserted_at", nullable = false)
    override var insertedAt: Instant = Instant.now()
        protected set
    @Column(name = "updated_at", nullable = false)
    override var updatedAt: Instant = Instant.now()
        protected set
    @Column(name = "is_deleted", nullable = false)
    override var isDeleted: Boolean = false
        protected set
    @Column(name = "deleted_at", nullable = true)
    override var deletedAt: Instant? = null
        protected set

    @Column(name = "last_fencing_token", nullable = true)
    override var lastFencingToken: Long = 0

    fun touch() {
        updatedAt = Instant.now()
    }

    @PreUpdate
    override fun preUpdate() {
        updatedAt = Instant.now()
    }

    override fun delete() {
        isDeleted = true
        deletedAt = Instant.now()
    }

    override fun restore() {
        isDeleted = false
        deletedAt = null
    }

}